# 生成勋章行动

通过[自定义代码模板](https://github.com/shuzijun/leetcode-editor/blob/master/doc/CustomCode_ZH.md)可以在一个项目中调试、运行、管理题目代码,同时也可以将这些代码文件进行保存与分享。此行动的主要功能是配合这个项目,生成刷题进度的徽章进行展示，以后也可以根据其他数据生成其他数据进行分享。

## 前提

使用此行动的前提是使用了[leetcode-editor](https://github.com/shuzijun/leetcode-editor)插件，并且推送的到github上的文件包括项目目录下.idea\leetcode(或者Pro版本的.idea\leetcode-pro)目录

## 配置


| 配置项               | 解释                       | 默认值                        |
| ---------------------- | ---------------------------- |----------------------------|
| COMMIT_MESSAGE       | 添加完勋章推送时填写的内容 | Update progress badge      |
| COMMIT_EMAIL         | 提交人的邮箱               | github-actions[bot]        |
| COMMIT_NAME          | 提交人姓名                 | leetcode-editor-bot        |
| BADGES_FILE          | 追加内容的文件             | README.md                  |
| START_SECTION_FLAG   | 追加内容区域的开始标识     | <\!--START_SECTION_FLAG--> |
| END_SECTION_FLAG     | 追加内容区域的结束标识     | <\!--END_SECTION_FLAG-->   |
| STATISTICS_DIRECTORY | 插件的数据文件目录         | .idea/leetcode/            |
| LEETCODE_SITE        | 力扣的站点                 | leetcode.com               |

## 示例

```yml
name: CI

on:
  push:
    branches: [ master ]
  pull_request:
    branches: [ master ]

  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - uses: ./action/
        with:
          STATISTICS_DIRECTORY: .idea/leetcode/
          LEETCODE_SITE: leetcode.cn
```

## 效果
![Progress](https://img.shields.io/static/v1?logo=leetcode&label=Progress&message=175%2F2643&color=brightgreen)  ![Easy](https://img.shields.io/static/v1?logo=leetcode&label=Easy&message=57&color=5CB85C)  ![Medium](https://img.shields.io/static/v1?logo=leetcode&label=Medium&message=106&color=F0AD4E)  ![Hard](https://img.shields.io/static/v1?logo=leetcode&label=Hard&message=12&color=D9534F)  