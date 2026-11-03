# javax.naming.ldap.InitialLdapContext 设计案

## 1. API 功能
`extends javax.naming.directory.InitialDirContext`、`implements LdapContext`。
- 构造器：`InitialLdapContext()`、`(Hashtable<?,?>, Control[])`。
- 方法：`extendedOperation`、`newInstance`、`reconnect`、`getConnectControls`、`setRequestControls`、`getRequestControls`、`getResponseControls`。

## 2. Java 用户如何使用
`new InitialLdapContext(env, connectControls)` 连接 LDAP 并获得 `LdapContext`。不提供环境时构造会尝试连接（本测试不连接网络，仅反射断言）。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/InitialLdapContext.java`。
- 来源：`~/git/jdk25u-dev/test/jdk` LDAP 测试用 `InitialLdapContext` 连接。

## 4. 测试用例设计
白盒（InitialLdapContextTest.testWhiteBox）：public/非接口/extends InitialDirContext/implements LdapContext + 构造器与方法存在性。不实例化。
