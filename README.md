# agora-call-1to1
基于对agora.io的通话模块的集成来实现一对一的网络语音通话<br>
# 项目效果预览
![](https://github.com/hanjie511/agora-call-1to1/raw/master/test.gif)<br>
# 项目说明
* 声网官网地址：https://www.agora.io/cn/  <br>
* 该项目主要实现了一对一的语音通话，并且。该应用基于其RTM模块实现了通话邀请功能和来电监听功能。<br>
# 项目文件说明
* MyService.java:来电监听和通话邀请的后台服务类。
* ProApplication.java:自定义的Application类，它主要实现了在程序启动时初始化一个全局共享的RtmClient对象，以便于后面的程序调用。
* LoginActivity.java:登录界面的activity。
* MainActivity.java:登录成功后进入的界面。
* VoiceCallActivity.java:来电和通话的界面。
* com.example.hj.call包下面的说有.java文件是为了代替服务端生成Token的所有工具类。（MyService.java除外）
* activity_voic_call.xml：通话和来电的布局界面。
* activity_main.xml：主界面的布局文件。
* activity_login.xml：登录界面的布局文件。
