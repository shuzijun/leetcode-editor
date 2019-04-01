# leetcode-editor   
  
  - [English Document](#Introduction)  
  - [中文文档](https://github.com/shuzijun/leetcode-editor/blob/master/README_ZH.md)   
  
## Introduction  
  Do Leetcode exercises in IDE, support `leetcode.com` and `leetcode-cn.com`, to meet the basic needs of doing exercises.
  Support theoretically: IntelliJ IDEA PhpStorm WebStorm PyCharm RubyMine AppCode CLion GoLand DataGrip Rider MPS Android Studio
 
  
## How to use  
<p align="center">
  <img src="https://github.com/shuzijun/leetcode-editor/blob/master/doc/leetcode-editor-3.0.gif" alt="demo"/>
</p>  

### Installation  
- **Install via plug-in library** https://plugins.jetbrains.com/plugin/12132-leetcode-editor  
- **Install by downloading the file** https://github.com/shuzijun/leetcode-editor/blob/master/doc/leetcode-editor.zip  

### Configuration (configuration for first installation)  

<p align="center">
  <img src="https://github.com/shuzijun/leetcode-editor/blob/master/doc/config-3.0.png" alt="settings"/>
</p>  
 
- **Configuration path**: `File` -> `settings`->`tools`->`leetcode plugin`  
  - **`URL options`**: `leetcode.com`OR`leetcode-cn.com`  
  - **`Code Type`**: `Java`,`Python`,`C++`,`Python3`,`C`,`C#`,`JavaScript`,`Ruby`,`Swift`,`Go` ,`Scala`,`Kotlin`,`Rust`,`PHP`   
  - **`LoginName`**: Login Username
  - **`Password`**: Login password  
  - **`Temp File Path`**: Temporary file storage catalogue  
  
### Window (Icon in the lower right corner of the main window![icon](https://github.com/shuzijun/leetcode-editor/blob/master/doc/LeetCodeIcon.png))  
  
<p align="center">
  <img src="https://github.com/shuzijun/leetcode-editor/blob/master/doc/window-3.0.png" alt="window"/>
</p>  
  
- **Toolbar**:  
  - ![login](https://github.com/shuzijun/leetcode-editor/blob/master/doc/login.png)**`Sign in`**:The login accounts of the two websites are not interoperable and the corresponding users need to be configured when switching websites.  
  - ![logout](https://github.com/shuzijun/leetcode-editor/blob/master/doc/logout.png)**`Sign out`**:Exit the current account, if you encounter a login error, try to exit first.  
  - ![refresh](https://github.com/shuzijun/leetcode-editor/blob/master/doc/refresh.png)**`Refresh`**:You can also refresh and load questions if you are not logged in, but you cannot submit it.  
  - ![find](https://github.com/shuzijun/leetcode-editor/blob/master/doc/find.png)**`Find`**:Input the content and press Enter to search , press again to search for the next one. It can only search under the question bank node.  
  - ![collapse](https://github.com/shuzijun/leetcode-editor/blob/master/doc/collapseAll.png)**`Collapse`**:Collapse all nodes.  
  - ![config](https://github.com/shuzijun/leetcode-editor/blob/master/doc/config.png)**`Settings`**:Quick jump to the configuration page.  
  - ![clear](https://github.com/shuzijun/leetcode-editor/blob/master/doc/clear.png)**`Clear`**:Clean up the files in the configured cache directories. The cache directories of the two websites are different and only the current configured websites are cleaned up. Carefully clean up cases without submitting.  

- **Tree**:  
  - **`Problems`**:All questions  
  - **`Difficulty`**:Classification of difficulties  
  - **`Tags`**:Classification of types  
  - **`Explore`**:Explore content, only contains titles of the questions, exclusive content that needs to pay ; some questions are loaded in order   
  - **`Color`**:The color represents the difficulty of the question  
  - **`Symbol`**:The symbols of “√” and “?” represent the status of the current questions, and explores with the beginning of “$” needs to pay or that cannot be viewed due to other causes.   
  
### Menu  
<p align="center">  
  <img src="https://github.com/shuzijun/leetcode-editor/blob/master/doc/menu-3.0.png" alt="menu"/>  
</p>   

- **Menu (right-click on the questions)**:  
  - **`open question`**:Open the question or double click on the question  
  - **`Submit`**:Submit the question  
  - **`Submissions`**:View the submission record, select the record details in the pop-up window(`Show detail`)  
  - **`Run Code`**:Run the code, the test case for the question is used by default  
  - **`Testcase`**:Customize test cases  
  - **`Clear cache`**:Clean up the current question  