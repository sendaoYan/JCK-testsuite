# javax.naming.ldap.UnsolicitedNotificationListener 设计案

## 1. API 功能
接口（`extends javax.naming.event.NamingListener`，即 `extends java.util.EventListener`）。方法：`notificationReceived(UnsolicitedNotificationEvent)`、继承 `namingExceptionThrown(NamingExceptionEvent)`。

## 2. Java 用户如何使用
实现监听器接收服务器主动通知；典型：`notificationReceived(evt)` 处理通知，`namingExceptionThrown(evt)` 处理错误。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/UnsolicitedNotificationListener.java`。
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/UnsolicitedNotificationEvent.java`。

## 4. 测试用例设计
白盒（UnsolicitedNotificationListenerTest.testWhiteBox）：public/接口/extends NamingListener/EventListener + 方法。
黑盒（testBlackBox）：匿名实现 + `UnsolicitedNotificationEvent.dispatch` 验证回调触发。
