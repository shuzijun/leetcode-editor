package com.shuzijun.leetcode.plugin.window.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.shuzijun.leetcode.plugin.model.Submission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author shuzijun
 */
public class SubmissionsPanel extends DialogWrapper {


    private JPanel jpanel;
    private JBTable table;

    public SubmissionsPanel(@Nullable Project project, TableModel tableModel) {
        super(project, true);
        jpanel = new JBPanel(new BorderLayout());
        jpanel.setMinimumSize(new Dimension(400, 400));
        jpanel.setPreferredSize(new Dimension(400, 400));
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
        jpanel.add(new JBScrollPane(table, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER),  BorderLayout.CENTER);

        setModal(true);
        init();
    }

    public void addTableMouseListener(MouseListener l) {
        this.table.addMouseListener(l);
    }

    public void addTableKeyListener(KeyListener l) {
        this.table.addKeyListener(l);
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return table;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return jpanel;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getCancelAction()};
    }

    public int getSelectedRow() {
        return table.getSelectedRow();
    }

    public static class TableModel extends AbstractTableModel {

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



