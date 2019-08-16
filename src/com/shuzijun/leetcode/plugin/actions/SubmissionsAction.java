package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.shuzijun.leetcode.plugin.manager.SubmissionManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Submission;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author shuzijun
 */
public class SubmissionsAction extends AbstractAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
        DefaultMutableTreeNode note = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Question question = (Question) note.getUserObject();

        List<Submission> submissionList = SubmissionManager.getSubmissionService(question);
        if (submissionList == null || submissionList.isEmpty()) {
            return;
        }
        SubmissionsAction.TableModel tableModel = new SubmissionsAction.TableModel(submissionList);
        SubmissionsPanel dialog = new SubmissionsPanel(anActionEvent.getProject(), tableModel);
        dialog.setTitle(question.getFormTitle() + " Submissions");

        if (dialog.showAndGet()) {
            SubmissionManager.openSubmission(submissionList.get(dialog.getSelectedRow()), question, anActionEvent.getProject());
        }
    }


    private class SubmissionsPanel extends DialogWrapper {


        private JPanel jpanel;
        private JBTable table;

        protected SubmissionsPanel(@Nullable Project project, SubmissionsAction.TableModel tableModel) {
            super(project, true);
            jpanel = new JBPanel();
            table = new JBTable(tableModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getTableHeader().setReorderingAllowed(false);
            table.setRowSelectionAllowed(true);
            table.setRowSelectionInterval(0, 0);
            table.getColumnModel().getColumn(0).setPreferredWidth(350);
            table.getColumnModel().getColumn(1).setPreferredWidth(200);
            table.getColumnModel().getColumn(2).setPreferredWidth(100);
            table.getColumnModel().getColumn(3).setPreferredWidth(200);
            table.getColumnModel().getColumn(4).setPreferredWidth(100);
            jpanel.add(new JBScrollPane(table, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

            setModal(true);
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            return jpanel;
        }

        @NotNull
        @Override
        protected Action getOKAction() {
            Action action = super.getOKAction();
            action.putValue(Action.NAME, "&Show detail");
            return action;
        }

        public int getSelectedRow() {
            return table.getSelectedRow();
        }
    }


    private class TableModel extends AbstractTableModel {

        String[] columnNames = {"Time", "Status", "Runtime", "Memory", "Language"};

        String[][] data = null;

        public TableModel(List<Submission> submissionList) {
            data = new String[submissionList.size()][columnNames.length];
            DateFormat sdf = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
            for (int i = 0, j = submissionList.size(); i < j; i++) {
                Submission s = submissionList.get(i);
                data[i][0] = sdf.format(new Date(Long.valueOf(s.getTime() + "000")));
                data[i][1] = s.getStatus();
                data[i][2] = s.getRuntime();
                data[i][3] = s.getMemory();
                data[i][4] = s.getLang();
            }
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
    }
}
