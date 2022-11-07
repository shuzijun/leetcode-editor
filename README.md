# Design类的题目

1. topicTags: {name: Design}

```
example cases
["LRUCache","put","put","get","put","get","put","get","get","get"]
[[2],[1,1],[2,2],[1],[3,3],[2],[4,4],[1],[3],[4]]

// 第一行 为方法
// 当作字符串解析
// 拿到class的名字
// 生成模板的时候外层class用wrapper包装

// 生成类似的模板

SkipList skipList = new SkipList(testcases[0]);
for i in range(1, n):
  if hasReturnValue:
    returnValue = 
  skipList.methods[i](testcases[i])
  if hasReturnValue:
    System.out.println("returnValue")

// 怎么知道returnValue的类型呢
// 怎么知道methodParam的类型呢
// 需要对codesnippet做解析    
```
