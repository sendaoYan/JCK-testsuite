# java.security.AccessControlContext 黑盒行为测试设计

## 1. API 功能
- 构造：`AccessControlContext(ProtectionDomain[])`、`AccessControlContext(AccessControlContext, DomainCombiner)`
- 查询：`getDomainCombiner()`、`equals`/`hashCode`
- 操作：`checkPermission(Permission)`（JDK-24 起 SecurityManager 移除后固定抛 AccessControlException）

## 2. Java 用户如何使用
用户一般不直接构造，而是经 `AccessController.getContext()` 获得"当前"快照，随后传给 `doPrivileged(action, acc)` 限制权限；或用 DomainCombiner 包装出新的上下文。链路：`getContext → checkPermission → doPrivileged(action, acc)`。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/AccessControlContext.java`：equals/hashCode 基于 domains 数组
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/AccessControlContext/NullCombinerEquals.java`（combiner 为 null 时 equals 语义）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/io/FilePermission/FilePermissionCollection.java`（ACC + PermissionCollection 组合使用）

## 4. 测试用例设计 + 自动化实现
- 正常：空域数组构造、两次 getContext 相等且 hashCode 一致、组合 combiner 后 getDomainCombiner 非空
- 组合：doPrivileged(action, acc) 正常返回受限结果
- 异常：checkPermission 抛 AccessControlException（JDK-27 固定行为），消息含 "checking permissions is not supported"

断言入口：`AccessControlContextTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
