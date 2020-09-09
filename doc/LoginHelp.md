# login config  
  - [中文文档](https://github.com/shuzijun/leetcode-editor/blob/master/doc/LoginHelp_ZH.md)   
  Login method: Configure username and password login, cookie login, browser login  
## Username password login  
  Configure the username and password on the configuration page of the leetcode plugin. If the login fails, the following two login methods will be loaded. This method only supports leetcode-cn.  
## cookie login  
  First log in to leetcode in your browser, open the browser console, copy the cookie to the login popup, and click login.[reference](https://developers.google.com/web/tools/chrome-devtools/network)   
  
  <p align="center">
    <img src="https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/browserCookie.png" alt="browserCookie"/>
  </p>  
  <p align="center">
    <img src="https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/cookieLogin.png" alt="cookieLogin"/>
  </p> 
    
## browser login   
  ~~This login requires additional download of dependent files. Download the compressed package with the version number ** jcef ** at the [releases](https://github.com/shuzijun/leetcode-editor/releases) address. After downloading, decompress it to the path of JCEFFilePath shown on the configuration page.~~  
  ~~If there is a resource file in the path, this login method will be used first, but this method is not compatible. If it cannot be loaded normally, you need to delete the contents of the folder and log in using other methods.~~  
  Starting from version 6.8, external mounting of JCEF is no longer supported. Instead, JCEF provided by JetBrains is used. The supported version is 2020.2+. If you meet the conditions of use, you can check JCEF in the configuration item.