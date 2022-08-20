package com.shuzijun.leetcode.extend.navigator;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.treeStructure.SimpleTree;
import com.shuzijun.leetcode.extension.NavigatorAction;
import com.shuzijun.leetcode.extension.NavigatorPanel;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.platform.model.*;
import com.shuzijun.leetcode.platform.notifier.ConfigNotifier;
import com.shuzijun.leetcode.platform.notifier.LoginNotifier;
import com.shuzijun.leetcode.platform.service.URLService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TreePanel extends NavigatorPanel implements Disposable {

    private final RepositoryService repositoryService;
    private final Map<String, Find> findMap = new HashMap<>();
    private final NavigatorAction<QuestionView> myNavigatorAction;
    private final SimpleTree tree;
    private final JBScrollPane contentScrollPanel;
    private final CustomTreeCellRenderer customTreeCellRenderer;
    private final ActionToolbar findToolbar;
    private final ActionToolbar actionSortToolbar;

    private final JPanel queryPanel;

    private final PageInfo<QuestionView> pageInfo = new PageInfo<>();


    public TreePanel(RepositoryService repositoryService) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.repositoryService = repositoryService;
        this.myNavigatorAction = createMyNavigatorAction();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new Question("root"));
        tree = new SimpleTree(new DefaultTreeModel(root)) {
            private final JTextPane myPane = new JTextPane();

            {
                myPane.setOpaque(false);
                myPane.replaceSelection("Click the icon to load");
            }

            @Override
            protected void paintComponent(Graphics g) {
                try {
                    super.paintComponent(g);
                }catch (Exception e){
                    return;
                }


                DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
                if (!root.isLeaf()) {
                    return;
                }

                myPane.setFont(getFont());
                myPane.setBackground(getBackground());
                myPane.setForeground(getForeground());
                Rectangle bounds = getBounds();
                myPane.setBounds(0, 0, bounds.width - 10, bounds.height);

                Graphics g2 = g.create(bounds.x + 10, bounds.y + 20, bounds.width, bounds.height);
                try {
                    myPane.paint(g2);
                } finally {
                    g2.dispose();
                }
            }
        };
        tree.getEmptyText().clear();

        tree.setOpaque(false);
        customTreeCellRenderer = new CustomTreeCellRenderer();
        customTreeCellRenderer.loaColor(repositoryService.getConfigService().getConfig());
        tree.setCellRenderer(customTreeCellRenderer);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);
        tree.addMouseListener(new TreeMouseListener(tree, repositoryService));

        final ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar;
        if (repositoryService.isPro()) {
            actionToolbar = actionManager.createActionToolbar("Leetcode.extend.Toolbar", (DefaultActionGroup) actionManager.getAction("leetcode.NavigatorActionsToolbar"), true);
        } else {
            actionToolbar = actionManager.createActionToolbar("Leetcode.pro.extend.Toolbar", (DefaultActionGroup) actionManager.getAction("leetcode.NavigatorActionsToolbar"), true);
        }

        actionToolbar.setTargetComponent(tree);
        setToolbar(actionToolbar.getComponent());

        SimpleToolWindowPanel toolWindowPanel = new SimpleToolWindowPanel(Boolean.TRUE, Boolean.TRUE);

        JPanel groupPanel = new JPanel();
        groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
        contentScrollPanel = new JBScrollPane(tree, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        groupPanel.add(contentScrollPanel);

        toolWindowPanel.setContent(groupPanel);

        queryPanel = new JPanel();
        queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.Y_AXIS));
        JTextField queryField = new JBTextField();
        queryField.setToolTipText("Enter Search");
        queryField.addKeyListener(new QueryKeyListener(queryField, myNavigatorAction, repositoryService.getProject()));
        queryPanel.add(queryField);

        findToolbar = actionManager.createActionToolbar("Leetcode.extend.find.Toolbar", (DefaultActionGroup) actionManager.getAction("leetcode.all.find.Toolbar"), true);
        findToolbar.setTargetComponent(tree);
        actionSortToolbar = actionManager.createActionToolbar("Leetcode.extend.find.Toolbar", (DefaultActionGroup) actionManager.getAction("leetcode.all.find.SortToolbar"), true);
        actionSortToolbar.setTargetComponent(tree);
        queryPanel.add(findToolbar.getComponent());
        queryPanel.add(actionSortToolbar.getComponent());

        queryPanel.setVisible(false);
        toolWindowPanel.setToolbar(queryPanel);
        setContent(toolWindowPanel);
        initFind();
        subscribe();

    }

    private void subscribe() {

        repositoryService.subscribeNotifier(Topic.LoginNotifier, new LoginNotifier() {
            @Override
            public void login(Project project, String host) {
                ProgressManager.getInstance().run(new Task.Backgroundable(repositoryService.getProject(), "Refresh data", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        if (!project.equals(myProject) && myNavigatorAction.getPageInfo().getRowTotal() <= 0) {
                            return;
                        }
                        myNavigatorAction.resetServiceData();
                    }
                });
            }

            @Override
            public void logout(Project project, String host) {
                ProgressManager.getInstance().run(new Task.Backgroundable(repositoryService.getProject(), "Refresh data", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        if (!project.equals(myProject) && myNavigatorAction.getPageInfo().getRowTotal() <= 0) {
                            return;
                        }
                        myNavigatorAction.resetServiceData();
                    }
                });
            }
        }, this);
        repositoryService.subscribeNotifier(Topic.ConfigNotifier, new ConfigNotifier() {
            @Override
            public void change(Config oldConfig, Config newConfig) {
                if (oldConfig != null && !oldConfig.getUrl().equalsIgnoreCase(newConfig.getUrl())) {
                    myNavigatorAction.loadServiceData();
                }
                customTreeCellRenderer.loaColor(newConfig);
            }
        }, this);
    }

    private void initFind() {
        Find cnFind = new Find();
        cnFind.addSort(Constant.SORT_TYPE_TITLE, new Sort(Constant.SORT_TYPE_TITLE, "TITLE"));
        cnFind.addSort(Constant.SORT_TYPE_DIFFICULTY, new Sort(Constant.SORT_TYPE_DIFFICULTY, "DIFFICULTY"));
        cnFind.addSort(Constant.SORT_TYPE_STATES, new Sort(Constant.SORT_TYPE_STATES, "STATES"));
        findMap.put(URLService.leetcodecn, cnFind);
        Find enFind = new Find();
        enFind.addSort(Constant.SORT_TYPE_TITLE, new Sort(Constant.SORT_TYPE_TITLE, "TITLE"));
        enFind.addSort(Constant.SORT_TYPE_DIFFICULTY, new Sort(Constant.SORT_TYPE_DIFFICULTY, "DIFFICULTY"));
        enFind.addSort(Constant.SORT_TYPE_STATES, new Sort(Constant.SORT_TYPE_STATES, "STATES"));
        findMap.put(URLService.leetcode, enFind);
    }

    @Override
    public NavigatorAction getNavigatorAction() {
        return myNavigatorAction;
    }


    private NavigatorAction<QuestionView> createMyNavigatorAction() {
        return new NavigatorAction.Adapter<>() {
            @Override
            public void updateUI() {
                findToolbar.getComponent().updateUI();
                actionSortToolbar.getComponent().updateUI();
            }

            @Override
            public JPanel queryPanel() {
                return queryPanel;
            }

            public PageInfo<QuestionView> getPageInfo() {
                return pageInfo;
            }


            @Override
            public boolean selectedRow(String slug) {
                if (StringUtils.isBlank(slug)) {
                    return true;
                }
                DefaultTreeModel treeMode = (DefaultTreeModel) tree.getModel();
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeMode.getRoot();
                if (root.isLeaf()) {
                    return true;
                }
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(0);
                if (node.isLeaf()) {
                    return true;
                }

                for (int i = 0, j = node.getChildCount(); i < j; i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                    QuestionView nodeData = (QuestionView) childNode.getUserObject();
                    if (nodeData.getTitleSlug().equals(slug)) {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            TreePath toShowPath = new TreePath(childNode.getPath());
                            tree.setSelectionPath(toShowPath);
                            Rectangle bounds = tree.getPathBounds(toShowPath);
                            if (bounds == null) {
                                return;
                            }
                            Point point = new Point(0, (int) bounds.getY());
                            JViewport viewport = contentScrollPanel.getViewport();
                            viewport.setViewPosition(point);
                        });
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Find getFind() {
                return findMap.get(repositoryService.getUrlService().getLeetcodeHost());
            }

            @Override
            public void findClear() {
                getFind().clearFilter();
                repositoryService.getViewService().loadAllServiceData(this);
            }

            @Override
            public void findChange(String filterKey, boolean b, Tag tag) {
                if ("categorySlug".equals(filterKey)) {
                    if (b) {
                        pageInfo.setCategorySlug(tag.getName());
                    } else {
                        pageInfo.setCategorySlug("");
                    }
                } else {
                    pageInfo.disposeFilters(filterKey, tag.getSlug(), b);
                }
                repositoryService.getViewService().loadAllServiceData(this);
            }

            @Override
            public void sort(Sort sort) {
                if (sort.getType() == 0) {
                    pageInfo.disposeFilters("orderBy", "", false);
                    pageInfo.disposeFilters("sortOrder", "", false);
                } else if (sort.getType() == 1) {
                    pageInfo.disposeFilters("orderBy", sort.getSlug(), true);
                    pageInfo.disposeFilters("sortOrder", "DESCENDING", true);
                } else if (sort.getType() == 2) {
                    pageInfo.disposeFilters("orderBy", sort.getSlug(), true);
                    pageInfo.disposeFilters("sortOrder", "ASCENDING", true);
                }
                repositoryService.getViewService().loadAllServiceData(this);
            }

            @Override
            public QuestionView getSelectedRowData() {
                QuestionView question = null;
                DefaultMutableTreeNode note = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (note != null) {
                    question = (QuestionView) note.getUserObject();
                    if (question != null) {
                        if ("lock".equals(question.getStatus())) {
                            question = null;
                        }
                    }
                }

                return question;
            }

            @Override
            public void loadData(String slug) {
                List<QuestionView> questionViewList = pageInfo.getRows();
                DefaultTreeModel treeMode = (DefaultTreeModel) tree.getModel();
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeMode.getRoot();
                root.removeAllChildren();
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Question(String.format("Problems(%d)", questionViewList.size())));
                root.add(node);
                for (QuestionView q : questionViewList) {
                    node.add(new DefaultMutableTreeNode(q));
                }
                DefaultMutableTreeNode listsNode = new DefaultMutableTreeNode(new Question(Constant.FIND_TYPE_LISTS));
                root.add(listsNode);
                addChild(listsNode,getFind().getFilter().get(Constant.FIND_TYPE_LISTS.toLowerCase()),questionViewList, (TagPredicate<QuestionView, Tag>) (q, t) -> t.getQuestions().contains(q.getQuestionId()));

                DefaultMutableTreeNode diffNode = new DefaultMutableTreeNode(new Question(Constant.FIND_TYPE_DIFFICULTY));
                root.add(diffNode);
                List<String> difficultyList = repositoryService.getFindService().getDifficulty().stream().map(Tag::getSlug).collect(Collectors.toList());
                addChild(diffNode, getFind().getFilter().get(Constant.FIND_TYPE_DIFFICULTY.toLowerCase()), questionViewList, (TagPredicate<QuestionView, Tag>) (q, t) -> {
                    Integer level = difficultyList.indexOf(t.getSlug()) + 1;
                    return q.getLevel().equals(level);
                });

                DefaultMutableTreeNode statusNode = new DefaultMutableTreeNode(new Question(Constant.FIND_TYPE_STATUS));
                root.add(statusNode);
                addChild(statusNode, getFind().getFilter().get(Constant.FIND_TYPE_STATUS.toLowerCase()), questionViewList, (TagPredicate<QuestionView, Tag>) (q, t) -> {
                    if ("TRIED".equalsIgnoreCase(t.getSlug()) && q.getStatusSign().equalsIgnoreCase("?")) {
                        return true;
                    } else if ("AC".equalsIgnoreCase(t.getSlug()) && q.getStatusSign().equalsIgnoreCase("✔")) {
                        return true;
                    } else if ("NOT_STARTED".equalsIgnoreCase(t.getSlug()) && (q.getStatusSign().equalsIgnoreCase("$") || StringUtils.isBlank(q.getStatusSign()))) {
                        return true;
                    }
                    return false;
                });

                DefaultMutableTreeNode tagsNode = new DefaultMutableTreeNode(new Question(Constant.FIND_TYPE_TAGS));
                root.add(tagsNode);
                addChild(tagsNode, getFind().getFilter().get(Constant.FIND_TYPE_TAGS.toLowerCase()), questionViewList, (TagPredicate<QuestionView, Tag>) (q, t) -> t.getQuestions().contains(q.getQuestionId()));


                tree.updateUI();
                treeMode.reload();
            }

            private void addChild(DefaultMutableTreeNode rootNode, List<Tag> Lists, List<QuestionView> questionViewList,TagPredicate tagPredicate) {
                if (Lists != null && !Lists.isEmpty()) {
                    for (Tag tag : Lists) {
                        List<QuestionView>  questionViews = questionViewList.stream().filter(questionView -> {
                            return tagPredicate.test(questionView,tag);
                        }).collect(Collectors.toList());
                        DefaultMutableTreeNode tagNode = new DefaultMutableTreeNode(new Question(String.format("%s(%d)",
                                tag.getName(), questionViews.size())));
                        rootNode.add(tagNode);
                        for (QuestionView questionView : questionViews) {
                                tagNode.add(new DefaultMutableTreeNode(questionView));
                        }
                    }

                }
            }

            @Override
            public void loadServiceData() {
                repositoryService.getViewService().loadAllServiceData(this);
            }

            @Override
            public void resetServiceData() {
                getFind().resetFilterData(Constant.FIND_TYPE_LISTS, repositoryService.getFindService().getLists());
                repositoryService.getViewService().loadAllServiceData(this, null, true);
            }

            @Override
            public boolean position(String slug) {
                if (StringUtils.isBlank(slug)) {
                    return true;
                }
                if (selectedRow(slug)) {
                    return true;
                }

                getFind().clearFilter();
                repositoryService.getViewService().loadAllServiceData(this, slug, false);
                return selectedRow(slug);
            }
        };
    }


    @Override
    public void dispose() {

    }

    @FunctionalInterface
    public interface TagPredicate<T1,T2> {
        boolean test(T1 t1,T2 t2);
    }
}
