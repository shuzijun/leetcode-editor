package com.shuzijun.leetcode.plugin.model.leetcode;

public class TreeNode {
  public int val;
  public TreeNode left;
  public TreeNode right;

  TreeNode() {}

  public TreeNode(int val) {
    this.val = val;
  }

  TreeNode(int val, TreeNode left, TreeNode right) {
    this.val = val;
    this.left = left;
    this.right = right;
  }

  public static int getTreeDepth(TreeNode root) {
    return root == null ? 0 : (1 + Math.max(getTreeDepth(root.left), getTreeDepth(root.right)));
  }

  private void writeArray(TreeNode currNode, int rowIndex, int columnIndex, String[][] res, int treeDepth) {
    if (currNode == null) {
      return;
    }
    res[rowIndex][columnIndex] = String.valueOf(currNode.val);

    int currLevel = ((rowIndex + 1) / 2);
    if (currLevel == treeDepth) {
      return;
    }
    int gap = treeDepth - currLevel - 1;
    if (currNode.left != null) {
      res[rowIndex + 1][columnIndex - gap] = "/";
      writeArray(currNode.left, rowIndex + 2, columnIndex - gap * 2, res, treeDepth);
    }

    if (currNode.right != null) {
      res[rowIndex + 1][columnIndex + gap] = "\\";
      writeArray(currNode.right, rowIndex + 2, columnIndex + gap * 2, res, treeDepth);
    }
  }

  public void print() {
    System.out.println(this);
  }

  @Override
  public String toString() {
    int treeDepth = getTreeDepth(this);
    int arrayHeight = treeDepth * 2 - 1;
    int arrayWidth = (2 << (treeDepth - 2)) * 3 + 1;
    String[][] res = new String[arrayHeight][arrayWidth];
    for (int i = 0; i < arrayHeight; i++) {
      for (int j = 0; j < arrayWidth; j++) {
        res[i][j] = " ";
      }
    }

    writeArray(this, 0, arrayWidth / 2, res, treeDepth);
    StringBuilder tmp = new StringBuilder();
    for (String[] line : res) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < line.length; i++) {
        sb.append(line[i]);
        if (line[i].length() > 1 && i <= line.length - 1) {
          i += line[i].length() > 4 ? 2 : line[i].length() - 1;
        }
      }
      tmp.append(sb);
    }
    return tmp.toString();
  }
}
