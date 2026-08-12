package com.shuzijun.leetcode.plugin.window.navigator;

import org.junit.Test;

import java.lang.reflect.Modifier;

import static org.junit.Assert.assertTrue;

public class NavigatorArchitectureTest {

    @Test
    public void exposesAProductNeutralNavigatorBaseAndConcretePanels() throws Exception {
        assertTrue(Modifier.isAbstract(NavigatorPanel.class.getModifiers()));
        assertTrue(NavigatorPanel.class.isAssignableFrom(SimpleNavigatorPanel.class));
        assertTrue(NavigatorPanel.class.isAssignableFrom(AllNavigatorPanel.class));
        assertTrue(NavigatorPanel.class.isAssignableFrom(TopNavigatorPanel.class));
    }
}
