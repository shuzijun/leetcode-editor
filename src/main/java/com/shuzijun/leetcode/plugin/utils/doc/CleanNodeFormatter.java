package com.shuzijun.leetcode.plugin.utils.doc;

import com.intellij.util.Url;
import com.intellij.util.Urls;
import com.intellij.util.io.URLUtil;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.HtmlBlock;
import com.vladsch.flexmark.ast.HtmlInline;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ext.attributes.AttributesNode;
import com.vladsch.flexmark.formatter.*;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author shuzijun
 */
public class CleanNodeFormatter implements NodeFormatter {

    public static final DataKey<String> FORMAT_HOST = new DataKey<>("FORMAT_HOST", "");

    private static final ThreadLocal<MutableDataHolder> threadOptions = ThreadLocal.withInitial(() -> new MutableDataSet());

    public static MutableDataHolder getThreadDataHolder() {
        return threadOptions.get();
    }

    public static void removeThreadDataHolder() {
        threadOptions.remove();
    }

    @Override
    public @Nullable Set<NodeFormattingHandler<?>> getNodeFormattingHandlers() {
        return new HashSet<>(Arrays.asList(
                new NodeFormattingHandler<>(FencedCodeBlock.class, CleanNodeFormatter.this::render),
                new NodeFormattingHandler<>(HtmlBlock.class, CleanNodeFormatter.this::render),
                new NodeFormattingHandler<>(HtmlInline.class, CleanNodeFormatter.this::render),
                new NodeFormattingHandler<>(AttributesNode.class, CleanNodeFormatter.this::render),
                new NodeFormattingHandler<>(InlineLaTexNode.class, CleanNodeFormatter.this::render),
                new NodeFormattingHandler<>(Image.class, CleanNodeFormatter.this::render)
        ));
    }


    private void render(@NotNull InlineLaTexNode texNode, @NotNull NodeFormatterContext nodeFormatterContext, @NotNull MarkdownWriter markdown) {
        if (texNode.getText().startsWith(texNode.getOpeningDelimiter())) {
            markdown.append(texNode.getText());
        } else {
            markdown.append(texNode.getOpeningDelimiter()).append(texNode.getText()).append(texNode.getClosingDelimiter());
        }
    }

    private void render(@NotNull AttributesNode attributesNode, @NotNull NodeFormatterContext nodeFormatterContext, @NotNull MarkdownWriter lineInfos) {
        if (attributesNode.getText().startsWith(":align") || attributesNode.getText().startsWith(":width")) {
            return;
        } else {
            nodeFormatterContext.delegateRender();
        }
    }

    private void render(FencedCodeBlock node, NodeFormatterContext context, MarkdownWriter markdown) {
        BasedSequence[] basedSequences = node.getInfo().split(" ");
        if (basedSequences.length > 1 && basedSequences[1].startsWith("[")) {
            node.setInfo(basedSequences[0]);
            markdown.blankLine();
            markdown.append("* ");
            for (int i = 1; i < basedSequences.length; i++) {
                markdown.append(basedSequences[i].toString());
            }
        } else {
            markdown.blankLine();
            markdown.append("* ").append(node.getInfo().toString());
        }
        if(node.getInfo().equals("python3",true)){
            node.setInfo(node.getInfo().subSequence(0,6));
        }
        context.delegateRender();
    }

    private void render(@NotNull HtmlBlock htmlBlock, @NotNull NodeFormatterContext context, @NotNull MarkdownWriter lineInfos) {
        htmlBlock.setChars(formatHtml(htmlBlock.getChars()));
        context.delegateRender();
    }

    private void render(HtmlInline node, NodeFormatterContext context, MarkdownWriter markdown) {
        if (node.getChars().startsWith("<video")) {
            markdown.append("<div> Video is not supported.");
        } else if (node.getChars().startsWith("</video")) {
            markdown.append("</div>");
        } else if (node.getChars().startsWith("<code")) {
            if (node.getParent() != null && node.getParent().getChars().startsWith("<pre")) {
                markdown.append("<span><code>");
            } else {
                context.delegateRender();
            }
        } else if (node.getChars().startsWith("</code")) {
            if (node.getParent() != null && node.getParent().getChars().startsWith("<pre")) {
                markdown.append("</code></span>");
            } else {
                context.delegateRender();
            }
        } else {
            context.delegateRender();
        }

    }

    private void render(Image node, NodeFormatterContext context, MarkdownWriter markdown) {
        String url = formatUrl(node.getPageRef().toString());
        node.setPageRef(BasedSequence.of(url));
        context.delegateRender();
    }

    @Override
    public @Nullable Set<Class<?>> getNodeClasses() {
        return null;
    }

    public static class Factory implements NodeFormatterFactory {
        public Factory() {
        }

        @NotNull
        public NodeFormatter create(@NotNull DataHolder options) {
            return new CleanNodeFormatter();
        }
    }

    private BasedSequence formatHtml(BasedSequence Html) {
        try {
            Document document = Parser.xmlParser().parseInput(new StringReader(Html.toString()), "");
            Elements elements = document.children();
            for (Element element : elements) {
                recursionElement(element);
            }
            return BasedSequence.of(document.html());
        } catch (Exception e) {
            return Html;
        }
    }

    private void recursionElement(Element element) {
        if ("img".equals(element.tagName())) {
            String src = element.attr("src");
            if (StringUtils.isNotBlank(src)) {
                element.attr("src", formatUrl(src));
            }
        } else if ("code".equals(element.tagName())) {
            if (element.parent() != null && "pre".equals(element.parent().tagName())) {
                Element span = new Element("span");
                element.before(span);
                element.remove();
                element.appendTo(span);
            }
        } else {
            Elements elements = element.children();
            for (Element element1 : elements) {
                recursionElement(element1);
            }
        }
    }

    private String formatUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            String host = FORMAT_HOST.get(threadOptions.get());
            if (url.startsWith("/")) {
                Url urlParse = Urls.parseEncoded(host);
                url = urlParse.getScheme() + URLUtil.SCHEME_SEPARATOR + urlParse.getAuthority() + url;
            } else {
                url = host + (host.endsWith("/") ? "" : "/") + url;
            }
        }
        url = url.replace("\\(", "(");
        url = url.replace("\\)", ")");
        return url;
    }
}
