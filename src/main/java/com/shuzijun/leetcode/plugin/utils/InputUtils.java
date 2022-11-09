package com.shuzijun.leetcode.plugin.utils;

import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.leetcode.ListNode;
import com.shuzijun.leetcode.plugin.model.leetcode.TreeNode;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaSource;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author arronshentu
 */
public class InputUtils {

  public static String generateTemplateCode(String path, String methodsString, String paramsString, Question q) {
    JavaProjectBuilder builder = new JavaProjectBuilder();
    JavaSource source = null;
    try {
      source = builder.addSource(new File(path));
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }
    JavaClass javaClass = source.getClasses().get(0);
    List<JavaMethod> methods = javaClass.getMethods();
    Map<String, JavaMethod> methodMap = new HashMap<>();
    for (JavaMethod method : methods) {
      methodMap.put(method.getName(), method);
    }
    String className = javaClass.getName();
    List<String> cases = Arrays.stream(stringToStringArray(methodsString)).collect(Collectors.toList());
    List<String> params = parseDesignParams(paramsString);
    String objectName = "instance";
    StringBuilder stringBuilder = new StringBuilder();
    StringBuilder printBuilder = new StringBuilder();
    String returnName = "value";
    int returnCount = 0;
    for (int i = 0; i < cases.size(); i++) {
      String c = cases.get(i);
      String p = params.get(i);
      if (className.equals(c)) {
        stringBuilder.append(className).append(" ").append(objectName).append(" = new ").append(VelocityTool.camelCaseName(q.getTitle())).append("().new ").append(className).append("(");
        stringBuilder.append(p).append(");");
        stringBuilder.append("\n");
      } else {
        JavaMethod method = methodMap.get(c);
        String returnType = method.getReturns().getName();
        if (!"void".equals(returnType)) {
          stringBuilder.append(returnType).append(" ").append(returnName).append(returnCount).append(" = ");
        }
        stringBuilder.append(objectName).append(".").append(c).append("(").append(p).append(");");
        stringBuilder.append("\n");
        if (!"void".equals(returnType)) {
          printBuilder.append("System.out.println(").append(returnName).append(returnCount++).append(");\n");
        }
      }
    }
    return stringBuilder.append("\n").append(printBuilder).toString();
  }

  public static List<String> parseDesignParams(String s) {
    Deque<Character> deque = new ArrayDeque<>();
    s = s.trim();
    int n = s.length();
    StringBuilder stringBuilder = new StringBuilder();
    List<String> res = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      char c = s.charAt(i);
      if (c == '[') {
        deque.addLast(c);
        stringBuilder.setLength(0);
      } else if (c == ']' && !deque.isEmpty()) {
        deque.pollLast();
        if (deque.isEmpty()) {
          break;
        }
        res.add(stringBuilder.toString());
        if (i + 1 < n && s.charAt(i + 1) == ',') {
          i++;
        }
      } else {
        stringBuilder.append(c);
      }
    }
    return res;
  }

  public static Object get(String testcase, Object type) {
    if (type instanceof String) {
      testcase = testcase.trim();
      if ("ListNode[].class".equals(type)) {
        return stringToLists(testcase);
      }
      if ("ListNode.class".equals(type)) {
        return stringToList(testcase);
      }
      if ("char[][].class".equals(type)) {
        return stringToCharArrays(testcase);
      }
      if ("int[][].class".equals(type)) {
        return stringToArrays(testcase);
      }
      if ("char.class".equals(type)) {
        if ("".equals(testcase)) {
          return ' ';
        } else {
          return testcase.replaceAll("\"", "").charAt(0);
        }
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
      if ("double.class".equals(type)) {
        return Double.parseDouble(testcase);
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
      if ("List<String>.class".equals(type)) {
        // todo List<?>.class
        return Arrays.stream(stringToStringArray(testcase)).collect(Collectors.toList());
      }
    }
    return null;
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

  public static int[] stringToArray(String input) {
    input = input.trim();
    if (input.startsWith("[") && input.endsWith("]")) {
      input = input.substring(1, input.length() - 1);
    }
    if (input.length() == 0) {
      return new int[0];
    }

    String[] parts = input.split(",");
    int[] output = new int[parts.length];
    for (int index = 0; index < parts.length; index++) {
      String part = parts[index].trim();
      output[index] = Integer.parseInt(part);
    }
    return output;
  }

  public static char[] stringToCharArray(String input) {
    input = input.trim();
    if (input.startsWith("[") && input.endsWith("]")) {
      input = input.substring(1, input.length() - 1);
    }
    if (input.length() == 0) {
      return new char[0];
    }

    String[] parts = input.split(",");
    char[] output = new char[parts.length];
    for (int index = 0; index < parts.length; index++) {
      String part = parts[index].trim();
      output[index] = part.charAt(1);
    }
    return output;
  }

  public static String[] stringToStringArray(String s) {
    s = process(s, "[", 1);
    String[] split = s.split(",");
    return Arrays.stream(split).map(t -> t.replaceAll("\"", "")).toArray(String[]::new);
  }

  public static char[][] stringToCharArrays(String s) {
    // [[1,2],[1,2],[1,2]]
    s = process(s, "[[", 2);
    if ("[]".equals(s)) {
      return new char[0][0];
    }
    String[] split = s.split("],\\[");
    List<char[]> res = new ArrayList<>();
    for (String s1 : split) {
      res.add(stringToCharArray(s1));
    }
    if (s.charAt(s.length() - 1) == '[') {
      res.add(new char[0]);
    }
    return res.toArray(new char[0][]);
  }

  public static int[][] stringToArrays(String s) {
    // [[1,2],[1,2],[1,2]]
    s = process(s, "[[", 2);
    if ("[]".equals(s)) {
      return new int[0][0];
    }
    String[] split = s.split("],\\[");
    List<int[]> res = new ArrayList<>();
    for (String s1 : split) {
      res.add(stringToArray(s1));
    }
    if (s.charAt(s.length() - 1) == '[') {
      res.add(new int[0]);
    }
    return res.toArray(new int[0][]);
  }

  public static TreeNode stringToTree(String s) {
    if ("[]".equals(s)) {
      return null;
    }
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
    if ("[]".equals(s)) {
      return ListNode.empty();
    }
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

  public static ListNode[] stringToLists(String s) {
    if ("[[]]".equals(s) || "[]".equals(s)) {
      return new ListNode[0];
    }
    s = process(s, "[", 1);
    // [1,2],[1,2]
    s = process(s, "[", 1);
    String[] split = s.split("],\\[");
    List<ListNode> list = new ArrayList<>();
    for (String s1 : split) {
      list.add(stringToList(s1));
    }
    return list.toArray(new ListNode[0]);
    // return (ListNode[])Arrays.stream(split).map(InputUtil::stringToList).toList().toArray(new Object[0]);
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
