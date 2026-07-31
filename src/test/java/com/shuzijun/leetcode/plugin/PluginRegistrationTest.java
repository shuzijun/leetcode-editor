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
}
