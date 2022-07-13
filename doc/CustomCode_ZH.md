# 自定义代码生成介绍  
  通过配置自定义代码生成模板可以自由生成代码格式，配合IDE可在本地调试代码。[示例工程](https://github.com/shuzijun/leetcode-question)  
  - [English Document](https://github.com/shuzijun/leetcode-editor/blob/master/doc/CustomCode.md)  
  - [中文文档](#配置)   
  <p align="center"><img src="https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/doc/customConfig-100.gif" alt="loacl" style="width: auto;height: auto;max-width: 90%; max-height: 90%;"></p>
 
  
## 配置  
<p align="center"><img src="https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/doc/config-3.0.jpg" alt="config" style="width: auto;height: auto;max-width: 90%; max-height: 90%;"></p>
  
  - **Custom code template**: 开启使用自定义模板，否则使用默认生成格式  
  - **CodeFileName**: 生成文件的名称，默认为题目标题  
  - **CodeTemplate**: 生成题目代码的内容，默认为题目描述和题目代码   
  - **TemplateConstant**： 模板常用变量  
    - **${question.title}**：题目标题，例如:两数之和  
    - **${question.titleSlug}**：题目标记，例如:two-sum  
    - **${question.frontendQuestionId}**：题目编号，例如:1  
    - **${question.content}**：题目描述内容  
    - **${question.code}**：题目代码部分  
    - **$!velocityTool.camelCaseName(str)**：一个函数，用来将字符串转化为驼峰样式  
  - 更多工具参考[VelocityTool.java](https://github.com/shuzijun/leetcode-editor/blob/master/src/main/java/com/shuzijun/leetcode/plugin/utils/VelocityTool.java)

## 注意  
  在生成的自定义代码中包含两行关键信息:  
  - `leetcode submit region begin(Prohibit modification and deletion)`:提交到leetcode进行验证的代码开始标记  
  - `leetcode submit region end(Prohibit modification and deletion)`:提交到leetcode进行验证的代码结束标记  
  这两行标记标示了提交到leetcode服务器进行验证的代码范围,在此范围内只允许有出现与题目解答相关的内容，出现其他内容可能导致leetcode验证不通过。  
  除了此范围内，其他区域是可以任意填写的，内容不会提交到leetcode，可以增加一些可以本地调试的内容，例如:import java.util.Arrays;  
  所以，这两行内容是不能被删除和修改的，否则将识别不到提交的内容。
  
## JAVA常用配置  
  可参考示例：[示例工程](https://github.com/shuzijun/leetcode-question)  
  CodeFileName:
  ```java
  $!velocityTool.camelCaseName(${question.titleSlug})
  ```
  TemplateConstant:
  ```java
    ${question.content}
    
    package com.shuzijun.leetcode.editor.en;
    public class $!velocityTool.camelCaseName(${question.titleSlug}){
        public static void main(String[] args) {
             Solution solution = new $!velocityTool.camelCaseName(${question.titleSlug})().new Solution();
        }
        ${question.code}
    }
  ```
