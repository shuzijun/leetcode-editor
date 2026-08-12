package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.leetcode.plugin.model.Tag;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

final class LcModelMapper {

    private LcModelMapper() {
    }

    @NotNull
    static Tag toTag(com.shuzijun.lc.model.Tag source, boolean translatedName) {
        Tag target = new Tag();
        target.setSlug(source.getSlug());
        target.setName(translatedName
                ? StringUtils.defaultIfBlank(source.getTranslatedName(), source.getName())
                : source.getName());
        target.setType(source.getType());
        copyQuestions(source.getQuestions(), target);
        return target;
    }

    private static void copyQuestions(Set<String> questionIds, Tag target) {
        if (questionIds == null) {
            return;
        }
        for (String questionId : questionIds) {
            target.addQuestion(questionId);
        }
    }
}
