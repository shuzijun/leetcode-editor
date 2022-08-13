package com.shuzijun.leetcode.plugin.utils.doc;

import com.vladsch.flexmark.ast.DelimitedNodeImpl;
import com.vladsch.flexmark.parser.core.delimiter.Delimiter;

/**
 * @author shuzijun
 */
public class InlineLaTexNode extends DelimitedNodeImpl {

    public static final String HTML_TEX = "LaTex";

    public static final String TOKEN_OPEN = "$";
    public static final String TOKEN_CLOSE = "$";

    private final String mOpener;
    private final String mCloser;

    public InlineLaTexNode(final Delimiter opener, final Delimiter closer) {
        mOpener = getDelimiter(opener);
        mCloser = getDelimiter(closer);
    }

    public String getOpeningDelimiter() {
        return mOpener;
    }

    public String getClosingDelimiter() {
        return mCloser;
    }

    private String getDelimiter(final Delimiter delimiter) {
        return delimiter.getInput().subSequence(
                delimiter.getStartIndex(), delimiter.getEndIndex()
        ).toString();
    }
}
