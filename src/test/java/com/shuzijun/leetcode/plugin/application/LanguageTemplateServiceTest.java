package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.spi.LanguageTemplateProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LanguageTemplateServiceTest {

    @Test
    public void rendersFileNameFromSelectedLanguageTemplate() {
        RecordingTemplateProvider provider = new RecordingTemplateProvider();
        Question question = question();

        String fileName = LanguageTemplateService.fileName(provider, "java", question);

        assertEquals("java", provider.lastFileNameLanguage);
        assertEquals("two-sum-java", fileName);
    }

    @Test
    public void rendersBodyFromSelectedLanguageTemplate() {
        RecordingTemplateProvider provider = new RecordingTemplateProvider();
        Question question = question();

        String body = LanguageTemplateService.template(provider, "python3", question);

        assertEquals("python3", provider.lastTemplateLanguage);
        assertEquals("python3:Two Sum", body);
    }

    private Question question() {
        Question question = new Question("Two Sum");
        question.setTitleSlug("two-sum");
        return question;
    }

    private static final class RecordingTemplateProvider implements LanguageTemplateProvider {
        private String lastFileNameLanguage;
        private String lastTemplateLanguage;

        @Override
        public String fileName(String languageSlug) {
            lastFileNameLanguage = languageSlug;
            return "${question.titleSlug}-" + languageSlug;
        }

        @Override
        public String template(String languageSlug) {
            lastTemplateLanguage = languageSlug;
            return languageSlug + ":${question.title}";
        }
    }
}
