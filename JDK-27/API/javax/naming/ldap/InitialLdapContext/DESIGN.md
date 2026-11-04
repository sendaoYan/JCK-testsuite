# javax.naming.ldap.InitialLdapContext 设计案

## 1. API 功能
初始 LDAP 上下文（extends InitialDirContext）。

## 2. Java 用户如何使用
new InitialLdapContext() 或 env+controls。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/InitialLdapContext.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造尝试（provider 决定）。
