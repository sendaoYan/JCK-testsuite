# javax.naming.directory.InitialDirContext 设计案

## 1. API 功能
`extends javax.naming.InitialContext`、`implements DirContext`。
- 构造器：`InitialDirContext()`、`(Hashtable<?,?>)`。
- 目录方法：`getAttributes`/`modifyAttributes`/`bind`/`rebind`/`createSubcontext`/`getSchema`/`getSchemaClassDefinition`/`search`（多重重载）。

## 2. Java 用户如何使用
`new InitialDirContext(env)`（env 含 `Context.INITIAL_CONTEXT_FACTORY`、`Context.PROVIDER_URL` 等）→ 获得 `DirContext` 后执行目录操作。不提供环境时构造会尝试连接默认目录（本测试不连接网络，仅做反射结构断言）。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/directory/InitialDirContext.java`。
- 来源：`~/git/jdk25u-dev/test/jdk` 的 JNDI/LDAP 用例用 `InitialDirContext` 连接目录服务。

## 4. 测试用例设计
白盒（InitialDirContextTest.testWhiteBox）：public/非接口/extends InitialContext/implements DirContext + 构造器与目录方法存在性。不实例化（避免真实连接）。
