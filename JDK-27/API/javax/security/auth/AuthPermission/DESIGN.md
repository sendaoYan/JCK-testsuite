# AuthPermission 设计案

## 1. API 功能
`javax.security.auth.AuthPermission`（JDK-27 已标记 `@Deprecated(forRemoval=true)`）继承
`java.security.BasicPermission`，表示“认证权限”。目标名（target name）即权限名，无独立动作列表：
- 构造器 `AuthPermission(String name)`、`AuthPermission(String name, String actions)`（actions 被忽略）。
- 继承的查询/判定：`getName()`、`getActions()`（恒为 `""`）、`implies(Permission)`、`equals`、`hashCode`。
- 向后兼容：构造入参 `"createLoginContext"` 会被改写为 `"createLoginContext.*"`。

来源：`~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/AuthPermission.java`。

## 2. Java 用户如何使用
```java
AuthPermission doAs = new AuthPermission("doAs");
doAs.getName();                       // "doAs"
doAs.getActions();                    // ""（无动作）
new AuthPermission("*").implies(doAs); // true（通配）
```
典型链路：`new AuthPermission(name) → getName / getActions → implies(另一权限) → equals/hashCode`。

## 3. 开源代码实际用法调研
- JDK 源码 `AuthPermission.java`：单参构造对 `createLoginContext` 做 `.*` 改写，双参构造把 actions 透传给 `BasicPermission`（BasicPermission 忽略 actions）。
- `BasicPermission` 的 `implies` 语义是 JDK 权威来源：`*` 蕴含一切，`a.*` 蕴含 `a.b` 但不蕴含精确 `a`。
- JDK 自带测试目录下无独立的 `AuthPermission` 测试（`grep -rl AuthPermission test/jdk` 命中的是 `java/security/PermissionCollection/Concurrent.java` 等借用该权限的用例）；本测试以 JDK 源码 + `BasicPermission` 契约为准，不凭记忆编造。

## 4. 测试用例设计 + 自动化实现
黑盒断言覆盖（见 `AuthPermissionTest.java` 的 `testBlackBox()`）：
- 名称/动作：`getName`、单参/双参 `getActions` 恒为空、非 null actions 仍为空。
- 通配蕴含：`*`→`doAs`、`doAs`→`*`、`doAs.*`→`doAs.sub`、`doAs.*`→`doAs`（反向为 false）。
- 向后兼容改写：`createLoginContext` → `createLoginContext.*` 且蕴含子权限。
- 等价/哈希：同名相等、hashCode 相等、异名不等。
- 边界/异常：空名 `IllegalArgumentException`、null 名 `NullPointerException`。
白盒保留原有行为断言并补齐：`public final`、父类 `BasicPermission`、`@Deprecated`。
