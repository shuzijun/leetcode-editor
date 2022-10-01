package com.shuzijun.leetcode.plugin.window.navigator;

import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.listener.JTableKeyAdapter;
import com.shuzijun.leetcode.plugin.listener.TreeMouseListener;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.model.PageInfo;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.QuestionView;
import com.shuzijun.leetcode.plugin.window.NavigatorTableData;
import icons.LeetCodeEditorIcons;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author shuzijun
 */
public class AllNavigatorTable extends NavigatorTableData<QuestionView> {


    private NavigatorAction myNavigatorAction;

    public AllNavigatorTable(Project project, NavigatorAction navigatorAction) {
        super(project);
        this.myNavigatorAction = navigatorAction;
    }

    @Override
    protected boolean dataNotifier(QuestionView myData, Question question) {
        if (myData.getTitleSlug().equals(question.getTitleSlug())) {
            myData.setStatus(question.getStatus());
            return true;
        }
        return false;
    }

    @Override
    protected PagePanel createMyPagePanel(PageInfo<QuestionView> myPageInfo, Project project) {
        return null;
    }

    @Override
    protected JTextPane firstToolTip() {
        List<Icon> icons = new ArrayList<>();
        icons.add(LeetCodeEditorIcons.CONFIG);
        icons.add(LeetCodeEditorIcons.LOGIN);
        icons.add(LeetCodeEditorIcons.REFRESH);
        icons.add(LeetCodeEditorIcons.TOGGLE);
        icons.add(LeetCodeEditorIcons.LOGIN);
        icons.add(LeetCodeEditorIcons.LOGOUT);
        icons.add(LeetCodeEditorIcons.REFRESH);
        icons.add(LeetCodeEditorIcons.RANDOM);
        icons.add(LeetCodeEditorIcons.FIND);
        icons.add(LeetCodeEditorIcons.POSITION);
        icons.add(LeetCodeEditorIcons.PROGRESS);
        icons.add(LeetCodeEditorIcons.TOGGLE);
        icons.add(LeetCodeEditorIcons.CONFIG);
        icons.add(LeetCodeEditorIcons.CLEAR);
        icons.add(LeetCodeEditorIcons.HELP);

        Style style = new StyleContext().addStyle("boldStyle", null);
        StyleConstants.setBold(style, true);
        List<MyStyle> styleList ;
        if(Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage())){
            styleList = Arrays.asList(new MyStyle(5, 7, style));
        }else {
            styleList = Arrays.asList(new MyStyle(18, 16, style));
        }
        return createTip("allTip", icons, styleList);
    }

    @Override
    protected MyJBTable<QuestionView> createMyTable(MyTableModel<QuestionView> myTableModel, Project project) {
        MyJBTable<QuestionView> myJBTable = new MyJBTable(myTableModel) {
            @Override
            protected void prepareRenderer(Component component, Object value, int row, int column) {
                if (component instanceof JLabel) {
                    if (column == 0 || column == 2) {
                        ((JLabel) component).setHorizontalAlignment(SwingConstants.CENTER);
                    } else {
                        ((JLabel) component).setHorizontalAlignment(SwingConstants.LEADING);
                    }
                }
                if (column == 2) {
                    if (value != null) {
                        if (value.toString().equals("Easy")) {
                            component.setForeground(Level1);
                        } else if (value.toString().equals("Medium")) {
                            component.setForeground(Level2);
                        } else if (value.toString().equals("Hard")) {
                            component.setForeground(Level3);
                        }
                    } else {
                        component.setForeground(defColor);
                    }
                } else {
                    component.setForeground(defColor);
                }
            }


        };

        myJBTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myJBTable.getTableHeader().setReorderingAllowed(false);
        myJBTable.setRowSelectionAllowed(true);
        myJBTable.setFillsViewportHeight(true);
        myJBTable.addMouseListener(new TreeMouseListener(this, project));
        myJBTable.addKeyListener(new JTableKeyAdapter(this, project));
        myJBTable.setShowGrid(false);
        return myJBTable;
    }

    @Override
    protected MyTableModel<QuestionView> createMyTableModel() {
        return new MyTableModel<QuestionView>(new String[]{"Status", "Title", "Difficulty"}, new String[]{"S", "Title", "DD"}) {
            @Override
            public Object getValue(QuestionView question, int columnIndex) {
                if (columnIndex == 0) {
                    return question.getStatusSign();
                }
                if (columnIndex == 1) {
                    return question.getFormTitle();
                }

                if (columnIndex == 2) {
                    Integer level = question.getLevel();
                    if (level == 1) {
                        return "Easy";
                    } else if (level == 2) {
                        return "Medium";
                    } else if (level == 3) {
                        return "Hard";
                    } else {
                        return level;
                    }
                }
                return null;
            }
        };
    }


    public boolean compareSlug(QuestionView myData, String titleSlug) {
        return myData.getTitleSlug().equals(titleSlug);
    }

    protected void setColumnWidth(MyJBTable myJBTable) {
        myJBTable.getColumnModel().getColumn(0).setMaxWidth(30);
        myJBTable.getColumnModel().getColumn(2).setMaxWidth(70);
    }

}
