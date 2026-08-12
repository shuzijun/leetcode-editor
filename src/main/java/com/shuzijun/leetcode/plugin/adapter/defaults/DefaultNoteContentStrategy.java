package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.spi.NoteContentStrategy;
import org.jetbrains.annotations.NotNull;

public final class DefaultNoteContentStrategy implements NoteContentStrategy {

    @Override
    public @NotNull String initialContent(Question question, CodeTypeEnum codeType) {
        return "";
    }
}
