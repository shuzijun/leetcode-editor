package com.shuzijun.leetcode.plugin;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PluginRegistrationTest {

    @Test
    public void loadsEveryRegisteredActionGroupAndToolWindowFactory() throws Exception {
        InputStream pluginXml = getClass().getResourceAsStream("/META-INF/plugin.xml");
        assertNotNull(pluginXml);

        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(pluginXml);
        assertClassAttributesLoad(document, "action");
        assertClassAttributesLoad(document, "group");
        assertClassAttributesLoad(document, "toolWindow");
        assertClassAttributesLoad(document, "fileEditorProvider");
    }

    @Test
    public void registersAllInteractiveActionsWithVisibleTextAndIcons() throws Exception {
        InputStream pluginXml = getClass().getResourceAsStream("/META-INF/plugin.xml");
        assertNotNull(pluginXml);

        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(pluginXml);
        assertEquals(65, document.getElementsByTagName("action").getLength());
        assertEquals(30, document.getElementsByTagName("group").getLength());
        assertEquals(95, countRegisteredActionIds(document));
        String[] actionIds = {
                "leetcode.LoginAction", "leetcode.LogoutAction", "leetcode.ClearAllAction",
                "leetcode.OpenInWebAction", "leetcode.SubmitAction", "leetcode.RunCodeAction",
                "leetcode.SubmissionsAction", "leetcode.FavoriteGroup", "leetcode.note",
                "leetcode.timer", "leetcode.positionAction", "leetcode.FindAction"
        };
        for (String actionId : actionIds) {
            Element action = findAction(document, actionId);
            assertNotNull("Missing UI action " + actionId, action);
            assertFalse("Missing visible text for " + actionId, action.getAttribute("text").isEmpty());
            assertFalse("Missing icon for " + actionId, action.getAttribute("icon").isEmpty());
        }

        String[] dynamicFilterGroupIds = {
                "leetcode.find.Difficulty", "leetcode.find.Status", "leetcode.find.Tags",
                "leetcode.find.Lists", "leetcode.codetop.find.Company"
        };
        for (String actionId : dynamicFilterGroupIds) {
            Element action = findAction(document, actionId);
            assertNotNull("Missing filter group " + actionId, action);
            assertFalse("Missing visible text for " + actionId, action.getAttribute("text").isEmpty());
            assertEquals("com.shuzijun.leetcode.plugin.actions.toolbar.FindActionGroup",
                    action.getAttribute("class"));
        }
        Class.forName("icons.LeetCodeEditorDefaultIcons");
        assertEquals(
                "LeetCodeEditorDefaultIcons.DONATE",
                findAction(document, "leetcode.DonateAction").getAttribute("icon")
        );
    }

    @Test
    public void registersProductNeutralArchitectureContracts() throws Exception {
        InputStream pluginXml = getClass().getResourceAsStream("/META-INF/plugin.xml");
        assertNotNull(pluginXml);

        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(pluginXml);
        assertExtensionPoint(
                document,
                "questionCatalog",
                "com.shuzijun.leetcode.plugin.spi.QuestionCatalogProvider"
        );
        assertExtensionPoint(
                document,
                "navigatorContribution",
                "com.shuzijun.leetcode.plugin.spi.NavigatorContribution"
        );
        assertExtensionPoint(
                document,
                "editorTabContribution",
                "com.shuzijun.leetcode.plugin.spi.EditorTabContribution"
        );
        assertExtensionPoint(
                document,
                "settingsSection",
                "com.shuzijun.leetcode.plugin.spi.SettingsSectionProvider"
        );
        assertImplementation(
                document,
                "questionCatalog",
                "com.shuzijun.leetcode.plugin.application.DefaultQuestionCatalogProvider"
        );
        assertImplementation(
                document,
                "navigatorContribution",
                "com.shuzijun.leetcode.plugin.adapter.defaults.DefaultPageNavigatorContribution"
        );
        assertImplementation(
                document,
                "navigatorContribution",
                "com.shuzijun.leetcode.plugin.adapter.defaults.DefaultAllNavigatorContribution"
        );
        assertImplementation(
                document,
                "navigatorContribution",
                "com.shuzijun.leetcode.plugin.adapter.defaults.DefaultCodeTopNavigatorContribution"
        );
        assertImplementation(
                document,
                "editorTabContribution",
                "com.shuzijun.leetcode.plugin.adapter.defaults.DefaultContentEditorTabContribution"
        );
        assertImplementation(
                document,
                "editorTabContribution",
                "com.shuzijun.leetcode.plugin.adapter.defaults.DefaultSolutionEditorTabContribution"
        );
        assertImplementation(
                document,
                "editorTabContribution",
                "com.shuzijun.leetcode.plugin.adapter.defaults.DefaultSubmissionsEditorTabContribution"
        );
        assertImplementation(
                document,
                "editorTabContribution",
                "com.shuzijun.leetcode.plugin.adapter.defaults.DefaultNoteEditorTabContribution"
        );
        assertImplementation(
                document,
                "settingsSection",
                "com.shuzijun.leetcode.plugin.adapter.defaults.DefaultSettingsSectionProvider"
        );
        assertApplicationService(
                document,
                "com.shuzijun.leetcode.plugin.product.DefaultPersistentConfig"
        );
        assertProjectService(
                document,
                "com.shuzijun.leetcode.plugin.product.DefaultProjectConfig"
        );
        assertProjectService(
                document,
                "com.shuzijun.leetcode.plugin.product.DefaultStatisticsData"
        );
        assertEquals(1, document.getElementsByTagName("applicationService").getLength());
        assertEquals(2, document.getElementsByTagName("projectService").getLength());
    }

    private Element findAction(Document document, String actionId) {
        NodeList actions = document.getElementsByTagName("*");
        for (int index = 0; index < actions.getLength(); index++) {
            Element action = (Element) actions.item(index);
            if (actionId.equals(action.getAttribute("id"))) {
                return action;
            }
        }
        return null;
    }

    private int countRegisteredActionIds(Document document) {
        int count = 0;
        NodeList elements = document.getElementsByTagName("*");
        for (int index = 0; index < elements.getLength(); index++) {
            Element element = (Element) elements.item(index);
            if (("action".equals(element.getTagName()) || "group".equals(element.getTagName()))
                    && !element.getAttribute("id").isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private void assertClassAttributesLoad(Document document, String elementName) throws ClassNotFoundException {
        NodeList elements = document.getElementsByTagName(elementName);
        assertFalse("Expected registered " + elementName + " elements", elements.getLength() == 0);
        for (int index = 0; index < elements.getLength(); index++) {
            Element element = (Element) elements.item(index);
            String className = element.getAttribute("class");
            if (className.isEmpty()) {
                className = element.getAttribute("factoryClass");
            }
            if (!className.isEmpty()) {
                Class.forName(className);
            }
        }
    }

    private void assertExtensionPoint(Document document, String name, String interfaceName) {
        NodeList elements = document.getElementsByTagName("extensionPoint");
        for (int index = 0; index < elements.getLength(); index++) {
            Element element = (Element) elements.item(index);
            if (name.equals(element.getAttribute("name"))) {
                assertEquals(interfaceName, element.getAttribute("interface"));
                return;
            }
        }
        throw new AssertionError("Missing extension point " + name);
    }

    private void assertImplementation(Document document, String elementName, String implementation) {
        NodeList elements = document.getElementsByTagName(elementName);
        for (int index = 0; index < elements.getLength(); index++) {
            Element element = (Element) elements.item(index);
            if (implementation.equals(element.getAttribute("implementation"))) {
                return;
            }
        }
        throw new AssertionError("Missing " + elementName + " implementation " + implementation);
    }

    private void assertApplicationService(Document document, String implementation) {
        assertRegisteredService(document, "applicationService", implementation);
    }

    private void assertProjectService(Document document, String implementation) {
        assertRegisteredService(document, "projectService", implementation);
    }

    private void assertRegisteredService(
            Document document,
            String elementName,
            String implementation
    ) {
        NodeList elements = document.getElementsByTagName(elementName);
        for (int index = 0; index < elements.getLength(); index++) {
            Element element = (Element) elements.item(index);
            if (implementation.equals(element.getAttribute("serviceImplementation"))) {
                return;
            }
        }
        throw new AssertionError("Missing " + elementName + " " + implementation);
    }
}
