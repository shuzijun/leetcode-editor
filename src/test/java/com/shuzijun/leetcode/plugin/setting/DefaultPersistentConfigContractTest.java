package com.shuzijun.leetcode.plugin.setting;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.openapi.components.State;
import com.shuzijun.leetcode.plugin.product.DefaultPersistentConfig;
import com.shuzijun.leetcode.plugin.product.DefaultProjectConfig;
import com.shuzijun.leetcode.plugin.product.DefaultStatisticsData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DefaultPersistentConfigContractTest {

    @Test
    public void keepsDefaultComponentStorageAndPasswordNamespaces() {
        State state = DefaultPersistentConfig.class.getAnnotation(State.class);

        assertNotNull(state);
        assertEquals("PersistentConfig", state.name());
        assertEquals(1, state.storages().length);
        assertEquals("leetcode-config.xml", state.storages()[0].value());
        State projectState = DefaultProjectConfig.class.getAnnotation(State.class);
        assertNotNull(projectState);
        assertEquals("LeetcodeEditor", projectState.name());
        assertEquals("leetcode/editor.xml", projectState.storages()[0].value());
        State statisticsState = DefaultStatisticsData.class.getAnnotation(State.class);
        assertNotNull(statisticsState);
        assertEquals("LeetcodeEditorStatistics", statisticsState.name());
        assertEquals("leetcode/statistics.xml", statisticsState.storages()[0].value());
        assertEquals(
                new CredentialAttributes("leetcode-editor", "user@example.com"),
                PersistentConfig.passwordAttributes("user@example.com")
        );
        assertEquals(
                new CredentialAttributes("leetcode-editor", "user@example.com", PersistentConfig.class),
                PersistentConfig.legacyPasswordAttributes("user@example.com")
        );
    }
}
