package com.shuzijun.leetcode.plugin.utils;

import java.util.*;
import java.util.stream.Collectors;

import com.shuzijun.leetcode.plugin.model.leetcode.ListNode;
import com.shuzijun.leetcode.plugin.model.leetcode.TreeNode;

/**
 * @author arronshentu
 */
public class InputUtils {
  public static Object get(String testcase, Object type) {
    if (type instanceof String) {
      if ("ListNode.class".equals(type)) {
        return stringToList(testcase);
      }
      if ("int[][].class".equals(type)) {
        return stringToArrays(testcase);
      }
      if ("int[].class".equals(type)) {
        return stringToArray(testcase);
      }
      if ("TreeNode.class".equals(type)) {
        return stringToTree(testcase);
      }
      if ("int.class".equals(type)) {
        return Integer.parseInt(testcase);
      }
      if ("String.class".equals(type)) {
        return testcase.replaceAll("\"", "");
      }
      if ("list<list<int>>.class".equals(type) || "List<List<Integer>>.class".equals(type)) {
        return stringToInt2dList(testcase);
      }
      if ("list<int>.class".equals(type) || "List<Integer>.class".equals(type)) {
        return stringToIntegerList(testcase);
      }
      if ("String[].class".equals(type)) {
        return stringToStringArray(testcase);
      }
    }
    return null;
  }

  public static String[] stringToStringArray(String s) {
    s = process(s, "[", 1);
    String[] split = s.split(",");
    return Arrays.stream(split).map(t -> t.replaceAll("\"", "")).toArray(String[]::new);
  }

  public static String[] param(String s) {
    // [integer[], integer[]]
    s = process(s, "[", 1);

    if (s.contains("list")) {
      s = s.replaceAll("list", "List");
      s = s.replaceAll("integer", "Integer");
      return Arrays.stream(s.split(", ")).map(t -> t + ".class").toArray(String[]::new);
    }

    s = s.replaceAll("integer", "int");
    s = s.replaceAll("string", "String");
    return Arrays.stream(s.split(", ")).map(t -> t + ".class").toArray(String[]::new);
  }

  private static String process(String s, String prefix, int x) {
    s = s.trim();
    if (s.startsWith(prefix)) {
      return s.substring(x, s.length() - x).trim();
    }
    return s;
  }

  public static int[] stringToArray(String s) {
    s = process(s, "[", 1);
    String[] split = s.split(",");
    return Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
  }

  public static int[][] stringToArrays(String s) {
    // [[1,2],[1,2],[1,2]]
    s = process(s, "[[", 2);
    String[] split = s.split("],\\[");
    List<int[]> res = new ArrayList<>();
    for (String s1 : split) {
      res.add(stringToArray(s1));
    }
    return res.toArray(new int[0][]);
  }

  public static TreeNode stringToTree(String s) {
    s = process(s, "[", 1);
    String[] parts = s.split(",");
    String item = parts[0];
    TreeNode root = new TreeNode(Integer.parseInt(item));
    // 1 1; 2 3; 3 1+2+4;
    Queue<TreeNode> nodeQueue = new LinkedList<>();
    nodeQueue.add(root);
    int index = 1;
    while (!nodeQueue.isEmpty()) {
      TreeNode node = nodeQueue.remove();

      if (index == parts.length) {
        break;
      }

      item = parts[index++];
      item = item.trim();
      if (!"null".equals(item)) {
        int leftNumber = Integer.parseInt(item);
        node.left = new TreeNode(leftNumber);
        nodeQueue.add(node.left);
      }

      if (index == parts.length) {
        break;
      }

      item = parts[index++];
      item = item.trim();
      if (!"null".equals(item)) {
        int rightNumber = Integer.parseInt(item);
        node.right = new TreeNode(rightNumber);
        nodeQueue.add(node.right);
      }
    }
    return root;
  }

  public static ListNode stringToList(String s) {
    // [[1,2],[1,2],[1,2]]
    s = process(s, "[", 1);
    String[] split = s.split(",");
    ListNode res = new ListNode(-1);
    ListNode index = res;
    for (String value : split) {
      index.next = new ListNode(Integer.parseInt(value));
      index = index.next;
    }
    return res.next;
  }

  public static List<Integer> stringToIntegerList(String s) {
    int[] array = stringToArray(s);
    return Arrays.stream(array).boxed().collect(Collectors.toList());
  }

  public static List<List<Integer>> stringToInt2dList(String s) {
    int[][] arrays = stringToArrays(s);
    List<List<Integer>> list = new ArrayList<>(arrays.length);
    for (int[] array : arrays) {
      list.add(Arrays.stream(array).boxed().collect(Collectors.toList()));
    }
    return list;
  }
}
