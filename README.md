# Extension for Java Code
Only works for java

## Recommendation

1. Set generate file tmp path like: YourProjectPath/src/edu/../algo/review
2. Use the following template
3. Set filename as your public class name
4. Install IntelliJ Plugin: Save actions
   1. set activate save actions on save as true
   2. set reformat file as true

### Template

```java
package edu.neu.algo.review.leetcode.editor.en.$!velocityTool.date("_yyyyMMdd");

public class $!velocityTool.camelCaseName(${question.title}){
  ${question.content}
  public static void main(String[]args){
    #if(${question.isDesign()})
    ${question.designCode}
    #else
    Solution solution=new $!velocityTool.camelCaseName(${question.title})().new Solution();
    String[] data="""
            ${question.exampleTestcases}""".trim().replaceAll("\n","|").split("\\|");
    String[]paramTypes=InputUtil.param("${question.paramTypes}");
    Object[]params=new Object[data.length];
    for(int i=0;i<data.length;i++){
      params[i]=InputUtil.get(data[i],paramTypes[i%paramTypes.length]);
    }
    int loop=data.length/paramTypes.length;
    for(int i=0;i<loop; i++){
      ${question.returnType}q=solution.${question.functionName}(
      #foreach($c in ${question.paramTypes})
      ($c)params[$foreach.count-1+i*paramTypes.length]
      #if($foreach.count<${question.paramTypes.size()}),
      #end
      #end);
      System.out.println(q);
    }
  #end
  }
  ${question.code}
}


```


# How it works?

1. Each leetcode design question has a tag: {name: design} and a 2-line testcase
   1. The first line contains methods, including constructor
   2. The second line contains their parameters 
2. Based on the code given by leetcode. We can know the method's return type, parameters
