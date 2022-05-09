# 登录配置  
  - [English Document](https://github.com/shuzijun/leetcode-editor/blob/master/doc/LoginHelp.md)  
  - [中文文档](#登录配置)   
  登录方式:配置用户名密码登录,cookie登录,浏览器登录  
## 用户名密码登录  
  在leetcode插件配置页配置用户名密码,如登录失败将加载下面两种登录方式,此方式仅支持leetcode.cn  
## cookie登录  
  首先在浏览器中登录leetcode,打开浏览器控制台,复制cookie到登录弹出框,点击login.[参考](https://developers.google.com/web/tools/chrome-devtools/network?hl=zh_cn)  
  <p align="center">
    <img src="https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/browserCookie.png" alt="browserCookie"/>
  </p>  
  <p align="center">
    <img src="https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/cookieLogin.png" alt="cookieLogin"/>
  </p>   
    
## 浏览器登录  
  ~~此登录需要额外下载依赖文件，在地址[releases](https://github.com/shuzijun/leetcode-editor/releases)下载版号带有**jcef**的压缩包,~~
  ~~下载后，解压到配置页展示的JCEFFilePath的路径中。~~  
  ~~如路径中存在资源文件,将首先使用此登录方式,但此方式兼容性差,如不能正常加载，需删除文件夹下内容,使用其他方式登录。~~  
  从版本6.8开始，不再支持外部挂载JCEF,改用JetBrains提供的JCEF,支持版本为2020.2+，如果满足使用条件，可以在配置项中勾选JCEF
