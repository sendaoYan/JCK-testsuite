# javax.naming.ldap.UnsolicitedNotificationEvent 设计案

## 1. API 功能
`extends java.util.EventObject`。构造器：`(Object src, UnsolicitedNotification notice)`。方法：`getNotification()`、`dispatch(UnsolicitedNotificationListener)`。

## 2. Java 用户如何使用
服务器主动通知以事件对象投递；典型：`new UnsolicitedNotificationEvent(src, notice)` → `getSource()/getNotification()` → `dispatch(listener)` 触发监听器回调。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/UnsolicitedNotificationEvent.java`。

## 4. 测试用例设计
黑盒断言（UnsolicitedNotificationEventTest.testBlackBox）：source/getNotification（含 null）；dispatch 触发监听器。
