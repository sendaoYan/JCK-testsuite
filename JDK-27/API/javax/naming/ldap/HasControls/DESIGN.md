# javax.naming.ldap.HasControls 设计案

## 1. API 功能
接口。方法：`getControls()`（返回 `Control[]`，throws `NamingException`）。

## 2. Java 用户如何使用
LDAP 消息（如 `UnsolicitedNotification`、搜索结果）可通过实现该接口暴露关联控件。典型：`if (entry instanceof HasControls) { Control[] c = ((HasControls)entry).getControls(); }`。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/HasControls.java`。
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/PagedResultsControl.java` 文档示例 `if (entry instanceof HasControls)`。

## 4. 测试用例设计
白盒（HasControlsTest.testWhiteBox）：public/接口 + getControls。
黑盒（testBlackBox）：匿名实现返回 `Control[]`，验证数组内容。
