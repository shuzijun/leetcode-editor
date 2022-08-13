package com.shuzijun.leetcode.plugin.utils.doc;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;

/**
 * @author shuzijun
 */
public class CleanExtensions implements Formatter.FormatterExtension, Parser.ParserExtension {

    public static CleanExtensions create() {
        return new CleanExtensions();
    }

    @Override
    public void rendererOptions(MutableDataHolder mutableDataHolder) {

    }

    @Override
    public void extend(Formatter.Builder builder) {
        builder.nodeFormatterFactory(new CleanNodeFormatter.Factory());
    }

    @Override
    public void parserOptions(MutableDataHolder options) {

    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new InlineLaTexProcessor());
    }
}
