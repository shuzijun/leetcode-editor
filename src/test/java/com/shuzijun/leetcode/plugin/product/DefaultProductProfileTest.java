package com.shuzijun.leetcode.plugin.product;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DefaultProductProfileTest {

    @Test
    public void exposesExistingDefaultIdentity() {
        ProductProfile profile = new DefaultProductProfile();

        assertEquals("12132", profile.marketplacePluginId());
        assertEquals("leetcode-editor", profile.pluginId());
        assertEquals("leetcode.editor", profile.pluginName());
        assertEquals("leetcode", profile.actionPrefix());
        assertEquals("", profile.actionSuffix());
        assertEquals("leetcode editor", profile.notificationGroup());
        assertEquals("Leetcode", profile.toolWindowId());
        assertEquals("Leetcode Console", profile.consoleToolWindowId());
        assertEquals("leetcode.id", profile.configurableId());
        assertEquals("LeetCode Plugin", profile.configurableDisplayName());
        assertEquals("leetcode", profile.configNamespace());
        assertEquals("lcv", profile.fileExtension());
        assertEquals("lcvDoc", profile.fileTypeName());
        assertEquals("lcvDoc", profile.languageId());
        assertEquals("leetcode-editor", profile.protocolNamespace());
        assertEquals("/leetcode/", profile.previewPathPrefix());
        assertEquals(
                "https://github.com/shuzijun/leetcode-editor/blob/master/CHANGELOG.md",
                profile.changelogUrl()
        );
        assertNull(profile.convergeEditorTypeId());
    }

    @Test
    public void publicLicenseAlwaysAllowsFeatures() {
        DefaultLicenseGate gate = new DefaultLicenseGate();

        assertTrue(gate.isAllowed());
        gate.onDenied();
    }
}
