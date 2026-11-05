# javax.naming.ldap.UnsolicitedNotification 设计案

## 1. API 功能
接口（`extends ExtendedResponse, HasControls`）。方法：`getReferrals()`、`getException()`；继承 `getID()`、`getEncodedValue()`、`getControls()`。

## 2. Java 用户如何使用
LDAP 服务器主动通知的抽象。典型：`n.getReferrals()`、`n.getException()`、`n.getControls()` 读取通知细节。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/UnsolicitedNotification.java`。
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/UnsolicitedNotificationEvent.java`。

## 4. 测试用例设计
白盒（UnsolicitedNotificationTest.testWhiteBox）：public/接口/extends ExtendedResponse+HasControls + 方法。
黑盒（testBlackBox）：匿名实现验证 getReferrals/getException/getID/getEncodedValue/getControls。
