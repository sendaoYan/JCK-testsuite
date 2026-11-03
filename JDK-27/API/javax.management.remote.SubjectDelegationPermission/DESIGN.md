# SubjectDelegationPermission 测试设计

## 1. API 功能
`javax.management.remote.SubjectDelegationPermission`（`final`，继承 `java.security.BasicPermission`）是配合 Security Manager 使用的权限类；JDK-25 起 `@Deprecated(forRemoval=true)`。

- 构造器：`(String name)`；`(String name, String actions)` —— `actions` 必须为 null，否则抛 `IllegalArgumentException`；null name 抛 `NullPointerException`。
- 查询：`getName()`、`getActions()`（恒为空串）、`implies()`、`equals/hashCode`（继承 `BasicPermission`）。

## 2. Java 用户如何使用
```java
SubjectDelegationPermission p = new SubjectDelegationPermission("javax.management.remote.SubjectDelegation");
p.getActions(); // ""
p.implies(new SubjectDelegationPermission("javax.management.remote.SubjectDelegation")); // true
```
典型组合链路：安全策略文件里声明 `permission javax.management.remote.SubjectDelegationPermission "name";`，运行时 `AccessController.checkPermission`。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management/share/classes/javax/management/remote/SubjectDelegationPermission.java`：`@Deprecated(since="25", forRemoval=true)`；二参构造对非 null actions 抛 `Non-null actions`；类文档注明 Security Manager 已不受支持故弃用。
来源为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
黑盒部分（`SubjectDelegationPermissionTest.testBlackBox()`）：
- 正常路径：name 保留、`getActions()` 为空串、`equals/hashCode`、`implies`。
- 组合/边界：二参构造 actions=null 合法；不同 name 不等。
- 异常（精确 message）：非 null actions → `Non-null actions`（IAE）；null name → `name can't be null`（NPE）。
（测试类加 `@SuppressWarnings("removal")` 抑制弃用告警。）
