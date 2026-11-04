# java.security.ProtectionDomain 黑盒行为测试设计

## 1. API 功能
- 构造：`ProtectionDomain(CodeSource, PermissionCollection)`、`ProtectionDomain(CodeSource, PermissionCollection, ClassLoader, Principal[])`
- 查询：`getCodeSource`/`getPermissions`/`getClassLoader`/`getPrincipals`（克隆）、`implies(Permission)`、`toString`

## 2. Java 用户如何使用
类→保护域→权限的绑定单元；`staticOnly` 构造的权限为快照，implies 先查静态权限再问策略。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/ProtectionDomain.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/lang/reflect/Proxy/Basic1.java`、`/data/yansendao/git/jdk25u-dev/test/jdk/java/rmi/reliability/benchmark/bench/rmi/ClassLoading.java`（类加载+保护域）

## 4. 测试用例设计 + 自动化实现
- 正常：两种构造器 getter 精确往返、getPrincipals 返回克隆（改副本不影响内部）
- 组合：AllPermission 域 implies 任意权限；空权限域不 implies；4 参构造 classLoader 读回
- 边界：toString 非空；permissions==null 时 implies 走默认策略

断言入口：`ProtectionDomainTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
