package com.shuzijun.leetcode.plugin.window.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.shuzijun.leetcode.plugin.model.Solution;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.List;

/**
 * @author shuzijun
 */
public class SolutionPanel extends DialogWrapper {

    private final JPanel jpanel;
    private final JBTable table;

    public SolutionPanel(@Nullable Project project, TableModel tableModel) {
        super(project, true);
        this.jpanel = new JBPanel(new BorderLayout());

        jpanel.setPreferredSize(new Dimension(600, 400));
        table = new JBTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setRowSelectionInterval(0, 0);
        table.getColumnModel().getColumn(0).setPreferredWidth(350);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(700);
        jpanel.add(new JBScrollPane(table, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

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

        String[] columnNames = {"Title", "Tags", "Summary"};

        String[][] data;

        public TableModel(List<Solution> solutionList) {
            data = new String[solutionList.size()][columnNames.length];
            for (int i = 0, j = solutionList.size(); i < j; i++) {
                Solution s = solutionList.get(i);
                data[i][0] = s.getTitle();
                data[i][1] = s.getTags();
                data[i][2] = s.getSummary();
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
