# javax.naming.ldap.LdapContext 设计案

## 1. API 功能
接口（`extends javax.naming.directory.DirContext`）。方法：`extendedOperation(ExtendedRequest)`、`newInstance(Control[])`、`reconnect(Control[])`、`getConnectControls()`、`setRequestControls(Control[])`、`getRequestControls()`、`getResponseControls()`。常量 `CONTROL_FACTORIES = "java.naming.factory.control"`。

## 2. Java 用户如何使用
由 `InitialLdapContext` 提供实现（需连接 LDAP）。典型：`extendedOperation(...)`、`setRequestControls`/`getResponseControls` 管理请求/响应控件。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/LdapContext.java`。
- 来源：`~/git/jdk25u-dev/test/jdk` LDAP 测试通过 `LdapContext` 接口编程。

## 4. 测试用例设计
白盒（LdapContextTest.testWhiteBox）：public/接口/extends DirContext + 常量值 + 方法存在性。不连接网络。
