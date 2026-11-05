# java.lang.RuntimePermission 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。BasicPermission 名称语义矩阵（exitVM 规范化为 exitVM.*、通配层级、裸 "*"）、
> 负例构造（null NPE / 空名 IAE，精确消息）、双参构造忽略 actions、equals/hashCode 值语义、
> toString 精确形、序列化 svuid 锁定、Guard/PermissionCollection 协作。断言确定，headless 安全，
> 不安装 SecurityManager。

## 1. API 功能

- **类**：`java.lang.RuntimePermission`（final，继承 `java.security.BasicPermission`）。
- **构造器**：`RuntimePermission(String name)`、`RuntimePermission(String name, String actions)`。
- **继承语义**：`getName/getActions/implies/equals/hashCode/newPermissionCollection`（来自 BasicPermission）。
- **语义**：名称是分层路径或保留字（`exitVM`、`stopThread`、`*`…）；`exitVM` 在内部规范化为 `exitVM.*`；actions 恒忽略。

## 2. Java 用户如何使用

- 与 `SecurityManager.checkPermission` / `AccessController.checkPermission` 配合表示运行时权限。

```java
RuntimePermission p = new RuntimePermission("exitVM");
p.implies(new RuntimePermission("exitVM.42")); // true：exitVM 已规范化为 exitVM.*
new RuntimePermission("");                      // IllegalArgumentException: name can't be empty
```

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/RuntimePermission.html`：目标名表；`BasicPermission.html`：名称规则）。
- **OpenJDK 8u-dev 源码** `jdk/src/share/classes/java/lang/RuntimePermission.java` 与 `java/security/BasicPermission.java`。
- **实测 JDK-8 运行时**（Temurin 8u372，探针 /tmp/lcF）：getName 规范化、异常消息文本、toString 形、serialVersionUID=7399184964622342223。

## 4. 测试用例设计（映射到 RuntimePermissionTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 基本构造/getName/getActions | testRuntimePermission |
| P0 | 6 名称 × 读写名、双参忽略 actions（含 null actions） | testNamesAndActions |
| P0 | null name NPE、空名 IAE（精确消息）、双参负例 | testNegativeConstruction |
| P0 | exitVM 规范化 / 通配层级 / 裸 "*" / implies(null)=false / 跨类型 / 自反 | testImpliesMatrix |
| P1 | equals/hashCode 值语义、异名不等 | testEqualsHashCode |
| P1 | toString 精确 `("java.lang.RuntimePermission" "exitVM")` | testToString |
| P1 | svuid 锁定 + round-trip（保留规范化名） | testSerialization |
| P2 | Guard.checkGuard、PermissionCollection add/implies/readOnly 后 add SE | testGuardAndCollection |
| P2 | final / 父类 BasicPermission / 恰 2 构造器 | testWhiteBox |

## 关键规范细节（易错，已锁入断言）

- **空字符串名称非法**：`IllegalArgumentException`，消息 `name can't be empty`（精确）；null name 是 `NullPointerException` `name can't be null`。
- **`new RuntimePermission("exitVM").getName()` 返回 `exitVM.*`**（已规范化）；它 implies `exitVM.5` 但**不** implies `exitVMX`。
- **`a.*` implies `a.b.c` 但不 implies `a`**；裸 `"*"` implies 一切；implies(null) 恒 false；implies 其它 Permission 类型恒 false。
- **`getActions()` 恒返回空串**——双参构造传入的 actions 被完全忽略。
- **serialVersionUID = 7399184964622342223**（JDK-8 与 JDK-27 相同，实测锁定）。

## 注意事项

- 全程不 `System.setSecurityManager`；PermissionCollection 的 `setReadOnly` 后 add 抛 SecurityException 是集合自身契约，与全局 SM 无关。
