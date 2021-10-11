package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.shuzijun.leetcode.plugin.listener.QuestionStatusListener;
import com.shuzijun.leetcode.plugin.listener.TreeMouseListener;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.PageInfo;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import icons.LeetCodeEditorIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class NavigatorTable extends JPanel {

    private static Color Level1 = new Color(92, 184, 92);
    private static Color Level2 = new Color(240, 173, 78);
    private static Color Level3 = new Color(217, 83, 79);
    private static Color defColor = null;

    private boolean first = true;

    private JBTable table;
    private MyTableModel tableModel;
    private Project project;

    private List<Question> questionList;
    private JComboBox page;
    private PageInfo<Question> pageInfo = new PageInfo<>(1, 50);


    public NavigatorTable(Project project) {
        super(new BorderLayout());
        this.project = project;
        loaColor();
        tableModel = new MyTableModel();
        table = new JBTable(tableModel) {
            @Override
            public String getToolTipText(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                String tipTextString = null;
                if (row > -1 && col == 1) {
                    Object value = table.getValueAt(row, col);
                    if (null != value && !"".equals(value))
                        tipTextString = value.toString();
                }
                return tipTextString;
            }

            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return MyTableModel.columnName[realIndex];
                    }
                };
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                if (first) {
                    if (row == 0 && column == 0) {
                        return firstToolTip();
                    } else {
                        return super.prepareRenderer(renderer, row, column);
                    }
                }
                Component component = super.prepareRenderer(renderer, row, column);
                if (defColor == null) {
                    synchronized (NavigatorTable.class) {
                        if (defColor == null) {
                            defColor = component.getForeground();
                        }
                    }
                }
                DefaultTableModel model = (DefaultTableModel) this.getModel();
                if (column == 3) {
                    Object value = model.getValueAt(row, column);
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
                return component;
            }
        };
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setFillsViewportHeight(true);
        table.addMouseListener(new TreeMouseListener(this, project));
        table.setRowHeight(0, 200);

        this.add(new JBScrollPane(table, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        this.add(paging(), BorderLayout.SOUTH);

        project.getMessageBus().connect().subscribe(QuestionStatusListener.QUESTION_STATUS_TOPIC, new QuestionStatusListener() {
            @Override
            public void updateTable(Question question) {
                if (questionList != null) {
                    for (Question q : questionList) {
                        if(q.getTitleSlug().equals(question.getTitleSlug())){
                            q.setStatus(question.getStatus());
                            refreshData();
                            break;
                        }
                    }
                }
            }
        });
    }

    public static void loaColor() {
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            Color[] colors = config.getFormatLevelColour();
            Level1 = colors[0];
            Level2 = colors[1];
            Level3 = colors[2];
        }
    }

    private Component paging() {
        JPanel paging = new JPanel(new BorderLayout());
        Integer[] pageSizeData = {20, 50, 100};
        JComboBox pageSizeBox = new JComboBox(pageSizeData);
        pageSizeBox.setPreferredSize(new Dimension(60, -1));
        pageSizeBox.setSelectedItem(50);
        pageSizeBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                pageInfo.setPageSize((Integer) e.getItem());
            }
        });
        paging.add(pageSizeBox, BorderLayout.WEST);

        JPanel control = new JPanel(new BorderLayout());
        JButton previous = new JButton("<");
        previous.setToolTipText("Previous");
        previous.setPreferredSize(new Dimension(50, -1));
        previous.setMaximumSize(new Dimension(50, -1));
        previous.addActionListener(event -> {
            if (page.getItemCount() <= 0 || (int) page.getSelectedItem() < 2) {
                return;
            } else {
                pageInfo.setPageIndex((int) page.getSelectedItem() - 1);
                NavigatorTable pNavigatorTable = this;
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Previous", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        ViewManager.loadServiceData(pNavigatorTable, project);
                    }
                });
            }

        });
        control.add(previous, BorderLayout.WEST);
        JButton next = new JButton(">");
        next.setToolTipText("Next");
        next.setPreferredSize(new Dimension(50, -1));
        next.setMaximumSize(new Dimension(50, -1));
        next.addActionListener(event -> {
            if (page.getItemCount() <= 0 || (int) page.getSelectedItem() >= page.getItemCount()) {
                return;
            } else {
                pageInfo.setPageIndex((int) page.getSelectedItem() + 1);
                NavigatorTable pNavigatorTable = this;
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Next", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        ViewManager.loadServiceData(pNavigatorTable, project);
                    }
                });
            }

        });
        control.add(next, BorderLayout.EAST);
        page = new JComboBox();
        control.add(page, BorderLayout.CENTER);
        paging.add(control, BorderLayout.CENTER);

        JButton go = new JButton("Go");
        go.setPreferredSize(new Dimension(50, -1));
        go.setMaximumSize(new Dimension(50, -1));
        go.addActionListener(event -> {
            if (page.getItemCount() <= 0) {
                return;
            } else {
                pageInfo.setPageIndex((int) page.getSelectedItem());
                NavigatorTable pNavigatorTable = this;
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Go to", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        ViewManager.loadServiceData(pNavigatorTable, project);
                    }
                });
            }

        });
        paging.add(go, BorderLayout.EAST);

        return paging;
    }

    public int getPageIndex() {
        if (page.getItemCount() <= 0) {
            return 1;
        } else {
            return (int) page.getSelectedItem();
        }
    }

    public PageInfo<Question> getPageInfo() {
        return pageInfo;
    }

    public void refreshData() {
        this.tableModel.updateData(questionList);
        setColumnWidth();
    }

    public void loadData(PageInfo<Question> pageInfo) {
        if (this.first) {
            this.tableModel.setRowCount(0);
            this.tableModel.setColumnCount(5);
            this.first = false;
        }
        this.questionList = pageInfo.getRows();
        this.tableModel.updateData(questionList);
        setColumnWidth();
        if (pageInfo.getPageTotal() != this.page.getItemCount()) {
            this.page.removeAllItems();
            for (int i = 1; i <= pageInfo.getPageTotal(); i++) {
                this.page.addItem(i);
            }
        }
        this.page.setSelectedItem(pageInfo.getPageIndex());
        this.pageInfo = pageInfo;
    }

    public Question getSelectedRowData() {
        int row = table.getSelectedRow();
        if (row < 0 || questionList == null || row >= questionList.size()) {
            return null;
        }
        return questionList.get(row);
    }

    private void setColumnWidth() {
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(2).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(60);
        table.getColumnModel().getColumn(4).setMaxWidth(50);
    }

    private JTextPane firstToolTip() {
        JTextPane myPane = new JTextPane();
        myPane.setOpaque(false);
        String addIconText = "'login'";
        String refreshIconText = "'refresh'";
        String configIconText = "'config'";
        String message = PropertiesUtils.getInfo("config.load", addIconText, refreshIconText, configIconText);
        int addIconMarkerIndex = message.indexOf(addIconText);
        myPane.replaceSelection(message.substring(0, addIconMarkerIndex));
        myPane.insertIcon(LeetCodeEditorIcons.LOGIN);
        int refreshIconMarkerIndex = message.indexOf(refreshIconText);
        myPane.replaceSelection(message.substring(addIconMarkerIndex + addIconText.length(), refreshIconMarkerIndex));
        myPane.insertIcon(LeetCodeEditorIcons.REFRESH);
        int configIconMarkerIndex = message.indexOf(configIconText);
        myPane.replaceSelection(message.substring(refreshIconMarkerIndex + refreshIconText.length(), configIconMarkerIndex));
        myPane.insertIcon(LeetCodeEditorIcons.CONFIG);
        myPane.replaceSelection(message.substring(configIconMarkerIndex + configIconText.length()));
        return myPane;
    }

    private static class MyTableModel extends DefaultTableModel {

        private NumberFormat nf = NumberFormat.getPercentInstance();

        public static String[] columnName = {"Status", "Title", "Acceptance", "Difficulty", "Frequency"};
        private static String[] columnNameShort = {"STAT", "Title", "AC", "DD", "F"};

        public MyTableModel() {
            super(new Object[]{"info"}, 1);
            nf.setMinimumFractionDigits(1);
            nf.setMaximumFractionDigits(1);
        }

        public Object getValue(Question question, int columnIndex) {
            if (columnIndex == 0) {
                return question.getStatusSign();
            }
            if (columnIndex == 1) {
                return question.getFormTitle();
            }

            if (columnIndex == 2) {
                return nf.format(question.getAcceptance());
            }

            if (columnIndex == 3) {
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
            if (columnIndex == 4) {
                return nf.format(question.getFrequency());
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public void updateData(List<Question> questionList) {
            if (questionList == null) {
                questionList = new ArrayList<>();
            }
            Object[][] dataVector = new Object[questionList.size()][columnName.length];
            for (int i = 0; i < questionList.size(); i++) {
                Object[] line = new Object[columnName.length];
                for (int j = 0; j < columnName.length; j++) {
                    line[j] = getValue(questionList.get(i), j);
                }
                dataVector[i] = line;
            }
            setDataVector(dataVector, MyTableModel.columnNameShort.clone());
        }
    }
}
