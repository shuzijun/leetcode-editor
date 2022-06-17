package com.shuzijun.leetcode.plugin.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    }
    return null;
  }

  public static String[] param(String s) {
    // [integer[], integer[]]
    s = process(s, "[", 1);
    s = s.replaceAll("integer", "int");
    return Arrays.stream(s.split(", ")).map(t -> t + ".class").toArray(String[]::new);
  }

  private static String process(String s, String prefix, int x) {
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

  static class ListNode {
    ListNode next;
    int val;

    public ListNode(int val) {
      this.val = val;
    }
  }
}
