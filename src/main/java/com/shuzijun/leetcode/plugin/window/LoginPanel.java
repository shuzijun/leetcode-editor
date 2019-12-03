package com.shuzijun.leetcode.plugin.window;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPanel;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefCookieVisitor;
import org.cef.handler.CefLoadHandler;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.misc.BoolRef;
import org.cef.network.CefCookie;
import org.cef.network.CefCookieManager;
import org.cef.network.CefRequest;
import org.jetbrains.annotations.Nullable;
import org.panda_lang.pandomium.Pandomium;
import org.panda_lang.pandomium.settings.PandomiumSettings;
import org.panda_lang.pandomium.util.FileUtils;
import org.panda_lang.pandomium.util.os.PandomiumOS;
import org.panda_lang.pandomium.wrapper.PandomiumBrowser;
import org.panda_lang.pandomium.wrapper.PandomiumClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class LoginPanel extends DialogWrapper {

    private static Pandomium pandomium;
    private static PandomiumClient client;
    private JPanel jpanel;
    final JFrame frame = new JFrame();
    private JTree tree;


    public LoginPanel(@Nullable Project project, JTree tree) {
        super(project, true);
        this.tree = tree;
        jpanel = new JBPanel();
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose();
                if (client != null) {
                    client.getCefClient().dispose();
                    client = null;
                }
            }
        });

        createClient();
        client.getCefClient().addLoadHandler(new CefLoadHandlerAdapter() {

            @Override
            public void onLoadStart(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest.TransitionType transitionType) {
                LogUtils.LOG.debug("onLoadEnd:" + cefBrowser.getURL());
                if (CefCookieManager.getGlobalManager() != null) {
                    final List<Cookie> cookieList = new ArrayList<Cookie>();
                    final Boolean[] isSession = {Boolean.FALSE};
                    boolean v = CefCookieManager.getGlobalManager().visitAllCookies(new CefCookieVisitor() {
                        @Override
                        public boolean visit(CefCookie cefCookie, int count, int total, BoolRef boolRef) {
                            if (cefCookie.domain.contains("leetcode")) {
                                BasicClientCookie cookie = new BasicClientCookie(cefCookie.name, cefCookie.value);
                                cookie.setDomain(cefCookie.domain);
                                cookie.setPath(cefCookie.path);
                                cookieList.add(cookie);
                                if ("LEETCODE_SESSION".equals(cefCookie.name)) {
                                    isSession[0] = Boolean.TRUE;
                                }
                            }
                            if (count == total - 1 && isSession[0]) {
                                HttpClientUtils.setCookie(cookieList);
                                if (HttpClientUtils.isLogin()) {
                                    ViewManager.loadServiceData(tree);
                                    examineEmail();
                                    cefFrame.executeJavaScript("alert('Login is successful. Please close the window')", "leetcode-editor", 0);
                                } else {
                                    LogUtils.LOG.info("login failure");
                                }
                            }
                            return true;
                        }
                    });

                }
            }


            @Override
            public void onLoadError(CefBrowser cefBrowser, CefFrame cefFrame, CefLoadHandler.ErrorCode errorCode, String s, String s1) {
                cefFrame.executeJavaScript("alert('页面加载失败，请检查网络后再次打开')", "leetcode-editor", 0);
            }
        });


        PandomiumBrowser browser = client.loadURL(URLUtils.getLeetcodeLogin());

        frame.getContentPane().add(browser.toAWTComponent(), BorderLayout.CENTER);

        frame.setTitle("login");
        frame.setSize(600, 400);
        frame.setVisible(true);
        jpanel.add(frame);
        setModal(true);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return jpanel;
    }

    private void createClient() {

        synchronized (this) {
            String path = PathManager.getPluginsPath() + File.separator + "leetcode-editor" + File.separator + "natives" + File.separator;

            if (!checkNative(new File(path))) {
                MessageUtils.showErrorMsg("login err", "natives Path is empty");
                throw new RuntimeException("natives Path is empty");
            }
            if (pandomium == null) {
                PandomiumSettings settings = PandomiumSettings.builder().nativeDirectory(path).loadAsync(false).build();
                pandomium = new Pandomium(settings);
                pandomium.initialize();
            }
            if (client == null) {
                client = pandomium.createClient();
            }
        }
    }

    @Override
    protected void dispose() {
        super.dispose();
    }

    private boolean checkNative(File directory) {
        if (!directory.exists()) {
            return false;
        } else if (!directory.isDirectory()) {
            FileUtils.handleFileResult(directory.delete(), "Couldn't delete directory %s", new Object[]{directory.getAbsolutePath()});
            return false;
        } else {
            File[] directoryContent = directory.listFiles();
            boolean success = FileUtils.isIn("libcef.so", directoryContent) || FileUtils.isIn("libcef.dll", directoryContent);
            if (PandomiumOS.isWindows()) {
                success = success && FileUtils.isIn("chrome_elf.dll", directoryContent) && FileUtils.isIn("jcef.dll", directoryContent);
            } else if (PandomiumOS.isLinux()) {
                success = success && FileUtils.isIn("cef.pak", directoryContent);
            }

            String cefHelperName = null;
            if (PandomiumOS.isMacOS()) {
                cefHelperName = "jcef Helper";
            } else if (PandomiumOS.isWindows()) {
                cefHelperName = "jcef_helper.exe";
            } else if (PandomiumOS.isLinux()) {
                cefHelperName = "jcef_helper";
            }

            if (cefHelperName != null && directoryContent != null) {
                File[] var5 = directoryContent;
                int var6 = directoryContent.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    File file = var5[var7];
                    if (file.getName().equals(cefHelperName)) {
                        file.setExecutable(true);
                        break;
                    }
                }
            }

            return success;
        }
    }

    private void examineEmail() {
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                HttpPost post = new HttpPost(URLUtils.getLeetcodeGraphql());
                try {
                    StringEntity entity = new StringEntity("{\"operationName\":\"user\",\"variables\":{},\"query\":\"query user {\\n  user {\\n    socialAccounts\\n    username\\n    emails {\\n      email\\n      primary\\n      verified\\n      __typename\\n    }\\n    phone\\n    profile {\\n      rewardStats\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}");
                    post.setEntity(entity);
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-type", "application/json");
                    CloseableHttpResponse response = HttpClientUtils.executePost(post);
                    if (response != null && response.getStatusLine().getStatusCode() == 200) {

                        String body = EntityUtils.toString(response.getEntity(), "UTF-8");

                        JSONArray jsonArray = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("user").getJSONArray("emails");
                        if (jsonArray != null && jsonArray.size() > 0) {
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                if (object.getBoolean("verified")) {
                                    return;
                                }
                            }

                        }
                        MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("user.email"));
                    }
                } catch (IOException i) {
                    LogUtils.LOG.error("验证邮箱错误");
                } finally {
                    post.abort();
                }
            }
        });
    }
}