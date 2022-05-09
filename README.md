# leetcode-editor [![Release](https://github.com/shuzijun/leetcode-editor/workflows/Release/badge.svg)](https://github.com/shuzijun/leetcode-editor/releases) [![Snapshot](https://github.com/shuzijun/leetcode-editor/workflows/Snapshot/badge.svg)](https://github.com/shuzijun/leetcode-editor/actions?query=workflow%3ASnapshot)   
  
  - [English Document](#Introduction)  
  - [中文文档](https://github.com/shuzijun/leetcode-editor/blob/master/README_ZH.md)   
  
  - Useful Links  
    - [Login Help](https://github.com/shuzijun/leetcode-editor/blob/master/doc/LoginHelp.md)  
    - [Custom Code](https://github.com/shuzijun/leetcode-editor/blob/master/doc/CustomCode.md) ([demo](https://github.com/shuzijun/leetcode-question))    
  
## Introduction  
  Do Leetcode exercises in IDE, support `leetcode.com` and `leetcode.cn`, to meet the basic needs of doing exercises.
  Support theoretically: IntelliJ IDEA PhpStorm WebStorm PyCharm RubyMine AppCode CLion GoLand DataGrip Rider MPS Android Studio.  
  <a href="http://shuzijun.cn/leetcode-editor/monitor.html" target="_blank">be with you!</a>
  
  
## How to use  
<p align="center">
  <img src="https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/leetcode-editor-3.0.gif" alt="demo"/>
</p>  

## Local debugging  
<p align="center">
  <img src="https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/customConfig-100.gif" alt="demo"/>
</p>  

### Installation([help](https://www.jetbrains.com/help/idea/2019.2/managing-plugins.html))  
- **Install via plug-in library** [leetcode-editor](https://plugins.jetbrains.com/plugin/12132-leetcode-editor)  
- **Install by downloading the file** [releases](https://github.com/shuzijun/leetcode-editor/releases)  
- **If you are willing to donate to this project, you can choose the pro version** [leetcode-editor-pro](https://plugins.jetbrains.com/plugin/17166-leetcode-editor-pro)  

### Configuration (configuration for first installation)  

<p align="center">
  <img src="https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/config-3.0.png" alt="settings"/>
</p>  
 
- **Configuration path**: `File` -> `settings`->`tools`->`leetcode plugin`  
  - **`URL options`**: `leetcode.com`OR`leetcode.cn`  
  - **`Code Type`**: `Java`,`Python`,`C++`,`Python3`,`C`,`C#`,`JavaScript`,`Ruby`,`Swift`,`Go` ,`Scala`,`Kotlin`,`Rust`,`PHP`,`Bash`,`SQL`   
  - **`LoginName`**: Login Username
  - **`Password`**: Login password  
  - **`Temp File Path`**: Temporary file storage catalogue  
  - **`proxy(HTTP Proxy)`**: HTTP Proxy,config path:`File` -> `settings`->`Appearance & Behavior`->`System Settings`->`HTTP Proxy`
  - **`Custom code template`**: Custom code template ([details](https://github.com/shuzijun/leetcode-editor/blob/master/doc/CustomCode.md)) ([demo](https://github.com/shuzijun/leetcode-question))  
  - **`LevelColour`**: Customize the difficulty color of the question, it will take effect after restart
  
### Window (Icon in the lower right corner of the main window![icon](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/LeetCodeIcon.png))  
  
<p align="center">
  <img src="https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/window-3.0.png" alt="window"/>
</p>  
  
- **Toolbar**:  
  - ![login](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/login.png)**`Sign in`**:The login accounts of the two websites are not interoperable and the corresponding users need to be configured when switching websites.  
  - ![logout](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/logout.png)**`Sign out`**:Exit the current account, if you encounter a login error, try to exit first.  
  - ![refresh](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/refresh.png)**`Refresh`**:You can also refresh and load questions if you are not logged in, but you cannot submit it.  
  - ![find](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/find.png)**`Find`**:Input the content and press Enter to search , press again to search for the next one. It can only search under the question bank node.  
  - ![collapse](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/collapseAll.png)**`Collapse`**:Collapse all nodes.  
  - ![config](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/config.png)**`Settings`**:Quick jump to the configuration page.  
  - ![clear](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/clear.png)**`Clear`**:Clean up the files in the configured cache directories. The cache directories of the two websites are different and only the current configured websites are cleaned up. Carefully clean up cases without submitting.  

- **Tree**:  
  - **`Problems`**:All questions  
  - **`Difficulty`**:Classification of difficulties  
  - **`Tags`**:Classification of types  
  - **`Explore`**:Explore content, only contains titles of the questions, exclusive content that needs to pay ; some questions are loaded in order   
  - **`Color`**:The color represents the difficulty of the question  
  - **`Symbol`**:The symbols of “√” and “?” represent the status of the current questions, and explores with the beginning of “$” needs to pay or that cannot be viewed due to other causes.   
  
### Menu  
<p align="center">  
  <img src="https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/menu-3.0.png" alt="menu"/>  
</p>   

- **Menu (right-click on the questions)**:  
  - **`open question`**:Open the question or double click on the question  
  - **`open content`**:Show content(Rely on Markdown)  
  - **`Submit`**:Submit the question  
  - **`Submissions`**:View the submission record, select the record details in the pop-up window(`Show detail`)  
  - **`Run Code`**:Run the code, the test case for the question is used by default  
  - **`Testcase`**:Customize test cases  
  - **`favorite`**:Add or remove favorite
  - **`Clear cache`**:Clean up the current question  
  - **`Timer`**:Timer, when it is turned on, it will prompt the problem solving time in the status bar in the lower right corner    

- **Editor Menu(Right-click on Editor to appear)**:  
  Function as above
