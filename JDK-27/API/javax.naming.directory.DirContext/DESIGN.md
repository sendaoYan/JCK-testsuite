# javax.naming.directory.DirContext 设计案

## 1. API 功能
接口（`extends javax.naming.Context`）。目录服务上下文，方法分组：
- 读取：`getAttributes(Name/String[, String[]])`。
- 修改：`modifyAttributes(Name/String, int, Attributes)`、`modifyAttributes(Name/String, ModificationItem[])`。
- 绑定：`bind/rebind(Name/String, Object, Attributes)`、`createSubcontext(Name/String, Attributes)`。
- Schema：`getSchema`、`getSchemaClassDefinition`。
- 搜索：`search`（多重重载）。
- 常量：`ADD_ATTRIBUTE=1`、`REPLACE_ATTRIBUTE=2`、`REMOVE_ATTRIBUTE=3`。

## 2. Java 用户如何使用
由 `InitialDirContext` 提供实现（需连接目录服务）。典型链路：bind → search → getAttributes → modifyAttributes。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/directory/DirContext.java`。
- 来源：`~/git/jdk25u-dev/test/jdk` 的 LDAP/DNS 目录测试通过 `DirContext` 接口编程。

## 4. 测试用例设计
白盒（DirContextTest.testWhiteBox）：public/接口/extends Context + 常量值 + 关键方法存在性。不连接真实网络。
