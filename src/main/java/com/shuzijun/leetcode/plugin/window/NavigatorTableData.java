package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.messages.MessageBusConnection;
import com.shuzijun.leetcode.plugin.listener.ConfigNotifier;
import com.shuzijun.leetcode.plugin.listener.QuestionStatusNotifier;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Graphql;
import com.shuzijun.leetcode.plugin.model.PageInfo;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.window.navigator.TopNavigatorTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author shuzijun
 */
public abstract class NavigatorTableData<T> extends JPanel implements Disposable {


    protected static volatile Color defColor = null;

    protected Color Level1 = new Color(92, 184, 92);
    protected Color Level2 = new Color(240, 173, 78);
    protected Color Level3 = new Color(217, 83, 79);

    private final MyJBTable<T> myTable;
    private final MyTableModel<T> myTableModel;
    private final Project project;
    private List<T> myList;
    private final PageInfo<T> myPageInfo;
    private final PagePanel myPagePanel;
    private final JComponent firstToolTip;
    private boolean first = true;

    public NavigatorTableData(Project project) {
        super(new BorderLayout());
        this.project = project;
        this.myTableModel = createMyTableModel();
        this.myTable = createMyTable(myTableModel, project);
        this.myPageInfo = createMyPageInfo();
        this.myPagePanel = createMyPagePanel(myPageInfo, project);
        this.firstToolTip = firstToolTip();
        this.add(firstToolTip, BorderLayout.CENTER);
        MessageBusConnection messageBusConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
        messageBusConnection.subscribe(ConfigNotifier.TOPIC, (oldConfig, newConfig) -> loaColor(newConfig));
        messageBusConnection.subscribe(QuestionStatusNotifier.QUESTION_STATUS_TOPIC, question -> {
            if (myList != null) {
                for (T q : myList) {
                    if (dataNotifier(q, question)) {
                        refreshData();
                        break;
                    }
                }

            }
        });

    }

    protected abstract boolean dataNotifier(T myData, Question question);

    protected PageInfo<T> createMyPageInfo() {
        return new PageInfo<>(1, 50);
    }

    protected abstract PagePanel createMyPagePanel(PageInfo<T> myPageInfo, Project project);

    protected abstract MyJBTable<T> createMyTable(MyTableModel<T> myTableModel, Project project);

    protected abstract MyTableModel<T> createMyTableModel();

    private void loaColor(Config config) {
        if (config != null) {
            Color[] colors = config.getFormatLevelColour();
            Level1 = colors[0];
            Level2 = colors[1];
            Level3 = colors[2];
        }
    }


    public T getSelectedRowData() {
        int row = myTable.getSelectedRow();
        if (row < 0 || myList == null || row >= myList.size()) {
            return null;
        }
        return myList.get(row);
    }


    public PageInfo<T> getPageInfo() {
        return myPageInfo;
    }

    private void refreshData() {
        ApplicationManager.getApplication().invokeLater(() -> {
            this.myTableModel.updateData(myList);
            setColumnWidth(myTable);
        });
    }

    public void refreshData(String selectTitleSlug) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (first) {
                this.remove(firstToolTip);
                this.add(new JBScrollPane(myTable, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
                if (myPagePanel != null) {
                    this.add(myPagePanel, BorderLayout.SOUTH);
                }
                first = false;
            }
            this.myList = myPageInfo.getRows();
            this.myTableModel.updateData(myList);
            setColumnWidth(myTable);
            myTable.requestFocusInWindow();
            if (selectTitleSlug != null) {
                selectedRow(selectTitleSlug);
            }
            if (myPagePanel != null) {
                if (myPageInfo.getPageTotal() != this.myPagePanel.page.getItemCount()) {
                    this.myPagePanel.page.removeAllItems();
                    for (int i = 1; i <= myPageInfo.getPageTotal(); i++) {
                        this.myPagePanel.page.addItem(i);
                    }
                }
                this.myPagePanel.page.setSelectedItem(myPageInfo.getPageIndex());
            }
        });

    }

    public boolean selectedRow(String titleSlug) {
        if (myList == null || myList.size() == 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            if (compareSlug(myList.get(i), titleSlug)) {
                int finalI = i;
                ApplicationManager.getApplication().invokeLater(() -> {
                    myTable.setRowSelectionInterval(finalI, finalI);
                    myTable.scrollRectToVisible(myTable.getCellRect(finalI, 0, true));
                });

                return true;
            }
        }
        return false;
    }

    public PagePanel getPagePanel() {
        return myPagePanel;
    }

    protected abstract void setColumnWidth(MyJBTable myJBTable);

    public abstract boolean compareSlug(T myData, String titleSlug);

    protected abstract JTextPane firstToolTip();

    @Override
    public void dispose() {
    }

    protected JTextPane createTip(String type, List<Icon> icons, List<MyStyle> styleList) {
        String cn = Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage()) ? "_cn" : "";
        JTextPane myPane = new JTextPane();
        myPane.setOpaque(false);
        try (InputStream inputStream = Graphql.GraphqlBuilder.class.getResourceAsStream("/template/" + type + cn + ".txt")) {
            if (inputStream == null) {
                LogUtils.LOG.error("/template/" + type + cn + ".txt Path is empty");
            } else {
                String templateTxt = new String(FileUtilRt.loadBytes(inputStream), StandardCharsets.UTF_8);
                int startIndex = 0;
                for (int i = 0; i < icons.size(); i++) {
                    String placeholder = "{" + i + "}";
                    int endIndex = templateTxt.indexOf(placeholder);
                    if (endIndex == -1) {
                        continue;
                    }
                    myPane.replaceSelection(templateTxt.substring(startIndex, endIndex));
                    myPane.insertIcon(icons.get(i));
                    startIndex = endIndex + placeholder.length();
                }
                myPane.replaceSelection(templateTxt.substring(startIndex));

                StyledDocument document = myPane.getStyledDocument();
                for (MyStyle myStyle : styleList) {
                    document.setCharacterAttributes(myStyle.offset, myStyle.length, myStyle.s, false);
                }
            }
        } catch (Exception e) {
            LogUtils.LOG.error("/template/" + type + cn + ".txt Loading exception", e);
        }
        return myPane;
    }

    protected static abstract class MyTableModel<T> extends DefaultTableModel {

        protected NumberFormat nf = NumberFormat.getPercentInstance();

        protected String[] columnName;
        protected String[] columnNameShort;

        public MyTableModel(String[] columnName, String[] columnNameShort) {
            super(new Object[]{"info"}, 1);
            this.columnName = columnName;
            this.columnNameShort = columnNameShort;
            nf.setMinimumFractionDigits(1);
            nf.setMaximumFractionDigits(1);
        }

        public abstract Object getValue(T question, int columnIndex);

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public void updateData(List<T> questionList) {
            if (questionList == null) {
                questionList = new ArrayList<>();
            }
            Object[][] dataVector = new Object[questionList.size()][columnName.length];
            for (int i = 0; i < questionList.size(); i++) {
                Object[] line = new Object[columnName.length];
                for (int j = 0; j < columnName.length; j++) {
                    line[j] = getValue(questionList.get(i), j);
                }
                dataVector[i] = line;
            }
            setDataVector(dataVector, columnNameShort);
        }
    }

    protected static abstract class MyJBTable<T> extends JBTable {

        private final MyTableModel<T> myTableModel;

        public MyJBTable(MyTableModel<T> model) {
            super(model);
            this.myTableModel = model;
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            int row = this.rowAtPoint(e.getPoint());
            int col = this.columnAtPoint(e.getPoint());
            String tipTextString = null;
            if (row > -1 && col == 1) {
                Object value = this.getValueAt(row, col);
                if (null != value && !"".equals(value)) tipTextString = value.toString();
            }
            return tipTextString;
        }

        @Override
        protected @NotNull JTableHeader createDefaultTableHeader() {
            return new JTableHeader(columnModel) {
                public String getToolTipText(MouseEvent e) {
                    Point p = e.getPoint();
                    int index = columnModel.getColumnIndexAtX(p.x);
                    int realIndex = columnModel.getColumn(index).getModelIndex();
                    return myTableModel.columnName[realIndex];
                }
            };
        }

        @Override
        public @NotNull Component prepareRenderer(@NotNull TableCellRenderer renderer, int row, int column) {
            Component component = super.prepareRenderer(renderer, row, column);
            if (defColor == null) {
                synchronized (TopNavigatorTable.class) {
                    if (defColor == null) {
                        defColor = component.getForeground();
                    }
                }
            }
            DefaultTableModel model = (DefaultTableModel) this.getModel();
            Object value = model.getValueAt(row, column);
            prepareRenderer(component, value, row, column);
            return component;
        }


        protected abstract void prepareRenderer(Component component, Object value, int row, int column);
    }

    public static abstract class PagePanel extends JBPanel {
        protected JComboBox<Integer> pageSizeBox;
        protected JButton previous;
        protected JButton next;
        protected JButton go;
        protected JComboBox<Integer> page;

        public PagePanel(Project project, PageInfo pageInfo) {
            super(new BorderLayout());
            pageSizeBox = new JComboBox(pageSizeData());
            pageSizeBox.setPreferredSize(new Dimension(60, -1));
            pageSizeBox.setSelectedItem(pageInfo.getPageSize());
            pageSizeBox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    pageInfo.setPageSize((Integer) e.getItem());
                }
            });
            add(pageSizeBox, BorderLayout.WEST);

            JPanel control = new JPanel(new BorderLayout());
            previous = new JButton("<");
            previous.setToolTipText("Previous");
            previous.setPreferredSize(new Dimension(50, -1));
            previous.setMaximumSize(new Dimension(50, -1));
            previous.addActionListener(event -> {
                if (page.getItemCount() <= 0 || (int) page.getSelectedItem() < 2) {
                } else {
                    pageInfo.setPageIndex((int) page.getSelectedItem() - 1);
                    ProgressManager.getInstance().run(new Task.Backgroundable(project, "Previous", false) {
                        @Override
                        public void run(@NotNull ProgressIndicator progressIndicator) {
                            previousRunnable();
                        }
                    });
                }

            });
            control.add(previous, BorderLayout.WEST);
            next = new JButton(">");
            next.setToolTipText("Next");
            next.setPreferredSize(new Dimension(50, -1));
            next.setMaximumSize(new Dimension(50, -1));
            next.addActionListener(event -> {
                if (page.getItemCount() <= 0 || (int) page.getSelectedItem() >= page.getItemCount()) {
                    return;
                } else {
                    pageInfo.setPageIndex((int) page.getSelectedItem() + 1);
                    ProgressManager.getInstance().run(new Task.Backgroundable(project, "Next", false) {
                        @Override
                        public void run(@NotNull ProgressIndicator progressIndicator) {
                            nextRunnable();
                        }
                    });
                }

            });
            control.add(next, BorderLayout.EAST);
            page = new JComboBox();
            control.add(page, BorderLayout.CENTER);
            add(control, BorderLayout.CENTER);

            go = new JButton("Go");
            go.setPreferredSize(new Dimension(50, -1));
            go.setMaximumSize(new Dimension(50, -1));
            go.addActionListener(event -> {
                if (page.getItemCount() <= 0) {
                    return;
                } else {
                    pageInfo.setPageIndex((int) page.getSelectedItem());
                    ProgressManager.getInstance().run(new Task.Backgroundable(project, "Go to", false) {
                        @Override
                        public void run(@NotNull ProgressIndicator progressIndicator) {
                            goRunnable();
                        }
                    });
                }

            });
            add(go, BorderLayout.EAST);
        }

        public abstract Integer[] pageSizeData();

        public abstract void previousRunnable();

        public abstract void nextRunnable();

        public abstract void goRunnable();

        public int getPageIndex() {
            if (page.getItemCount() <= 0) {
                return 1;
            } else {
                return (int) page.getSelectedItem();
            }
        }

        public void focusedPageSize() {
            pageSizeBox.requestFocusInWindow();
        }

        public void focusedPage() {
            page.requestFocusInWindow();
        }

        public void clickPrevious() {
            previous.doClick();
        }

        public void clickNext() {
            next.doClick();
        }

        public void clickGo() {
            go.doClick();
        }
    }

    public static class MyStyle {
        private int offset;
        private int length;
        private AttributeSet s;

        public MyStyle(int offset, int length, AttributeSet s) {
            this.offset = offset;
            this.length = length;
            this.s = s;
        }
    }
}
