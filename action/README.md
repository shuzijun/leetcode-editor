# Generate Badge Action

By [customizing the code template](https://github.com/shuzijun/leetcode-editor/blob/master/doc/CustomCode.md), you can debug, run, and manage the subject code in a project, and you can also save and share these code files. The main function of this action is to cooperate with this project to generate badges for the progress of brushing questions for display, and other data can also be generated and shared based on other data in the future.

## Prerequisites

The premise of using this action is to use the [leetcode-editor](https://github.com/shuzijun/leetcode-editor) plugin, and the files pushed to github include the .idea\leetcode (or Pro version .idea\leetcode-pro) directory in the project directory

## configure


|  Configuration item    | Explanation                                     |  Default value             |
| ---------------------- |-------------------------------------------------|----------------------------|
| COMMIT_MESSAGE         | Content to be filled in after adding badge push | Update progress badge |
| COMMIT_EMAIL | Committer's email address                       | github-actions[bot] |
| COMMIT_NAME | Committer name                                  | leetcode-editor-bot |
| BADGES_FILE | Append content file                             | README.md |
| START_SECTION_FLAG | Start flag of append content area               | <\!--START_SECTION_FLAG--> |
| END_SECTION_FLAG | End marker of additional content area           | <\!--END_SECTION_FLAG--> |
| STATISTICS_DIRECTORY | Plugin's data file directory                    | .idea/leetcode/ |
| LEETCODE_SITE | Leetcode's site                                 | leetcode.com |

## Example

````yml
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
````

## Effect
![Progress](https://img.shields.io/static/v1?logo=leetcode&label=Progress&message=175%2F2643&color=brightgreen) ![Easy](https://img.shields.io/static/v1?logo=leetcode&label=Easy&message=57&color=5CB85C) ![Medium](https://img.shields.io/static/v1?logo=leetcode&label=Medium&message=106&color=F0AD4E)  ![Hard](https://img.shields.io/static/v1?logo=leetcode&label=Hard&message=12&color=D9534F)