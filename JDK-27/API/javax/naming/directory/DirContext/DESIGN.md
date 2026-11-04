# javax.naming.directory.DirContext 设计案

## 1. API 功能
目录上下文接口（extends Context）：attributes/子上下文/搜索 + 三个属性常量。

## 2. Java 用户如何使用
需要 LDAP/DNS 服务提供者。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/directory/DirContext.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface+常量；黑盒降级（需网络）。
