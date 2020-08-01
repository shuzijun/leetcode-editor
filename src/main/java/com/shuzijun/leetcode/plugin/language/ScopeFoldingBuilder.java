package com.shuzijun.leetcode.plugin.language;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.shuzijun.leetcode.plugin.model.Constant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author shuzijun
 */
public class ScopeFoldingBuilder extends FoldingBuilderEx implements DumbAware {
    @Override
    public @NotNull FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean b) {
        FoldingGroup group = FoldingGroup.newGroup("leetcode editor scope");
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        // Get a collection of the literal expressions in the document below root
        Collection<PsiComment> psiComments =
                PsiTreeUtil.findChildrenOfType(root, PsiComment.class);
        // Evaluate the collection
        for (final PsiComment psiComment : psiComments) {
            String value = psiComment.getText();
            if (value != null && (value.contains(Constant.SUBMIT_REGION_BEGIN) || value.contains(Constant.SUBMIT_REGION_END))) {
                // Add a folding descriptor for the literal expression at this node.
                descriptors.add(new FoldingDescriptor(psiComment.getNode(),
                        new TextRange(psiComment.getTextRange().getStartOffset(),
                                psiComment.getTextRange().getEndOffset()),
                        group));
            }
        }
        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }


    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode astNode) {
        String retTxt = "----";
        if (astNode.getPsi() instanceof PsiComment) {
            PsiComment psiComment = (PsiComment) astNode.getPsi();
            String value = psiComment.getText();
            if (value != null) {
                if (value.contains(Constant.SUBMIT_REGION_BEGIN)) {
                    retTxt = "--BEGIN--";
                } else if (value.contains(Constant.SUBMIT_REGION_END)) {
                    retTxt = "--END--";
                }
            }
        }
        return retTxt;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode astNode) {
        return true;
    }
}
