package com.shuzijun.leetcode.plugin.manager;

import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Question;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class NoteManagerTest {

    @Test
    public void keepsExplicitLanguageEntryPointsForProductActions() throws Exception {
        Method show = NoteManager.class.getMethod(
                "show",
                String.class,
                com.intellij.openapi.project.Project.class,
                Boolean.class,
                CodeTypeEnum.class
        );
        Method pull = NoteManager.class.getMethod(
                "pull",
                String.class,
                com.intellij.openapi.project.Project.class,
                CodeTypeEnum.class
        );

        assertEquals(java.io.File.class, show.getReturnType());
        assertEquals(boolean.class, pull.getReturnType());
    }
}
