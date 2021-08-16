package com.shuzijun.leetcode.plugin.utils.doc;

import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.Arrays;

/**
 * @author shuzijun
 */
public class CleanMarkdown {
    final private static DataHolder OPTIONS = new MutableDataSet()
            .set(Parser.EXTENSIONS, Arrays.asList(CleanExtensions.create(), AttributesExtension.create()))
            .set(Parser.LISTS_AUTO_LOOSE, false)
            .toImmutable();

    static final Parser PARSER = Parser.builder(OPTIONS).build();
    static final Formatter RENDERER = Formatter.builder(OPTIONS).build();

    public static String cleanMarkdown(String markdown, String host) {
        try {
            CleanNodeFormatter.getThreadDataHolder().set(CleanNodeFormatter.FORMAT_HOST, host);
            Node document = PARSER.parse(markdown);
            return RENDERER.render(document);
        } finally {
            CleanNodeFormatter.removeThreadDataHolder();
        }
    }

}
