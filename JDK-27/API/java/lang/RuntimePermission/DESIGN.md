# RuntimePermission — 设计方案

## 1. API 功能
`java.lang.RuntimePermission`（final，继承 `BasicPermission`）是运行时权限：构造器 `(String name)`、`(String name, String actions)`，继承 `getName/getActions/implies/equals/hashCode`。

## 2. Java 用户如何使用
- 与 `SecurityManager`/`AccessController` 配合表示权限（如 `getClassLoader`、`setSecurityManager`）。
```java
RuntimePermission p = new RuntimePermission("getClassLoader");
boolean allowed = p.implies(p);
```

## 3. 开源代码实际用法调研
- 旧式安全管理器检查点使用；JDK `test/jdk/java/lang/RuntimePermission` 验证名称/actions/implies。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证 final、父类 `BasicPermission`、两个构造器。
黑盒：名称、`BasicPermission` 忽略 actions（`getActions` 恒为空）、`implies` 同名、`equals/hashCode` 值语义。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）

- **BasicPermission 名称语义矩阵**：`exitVM` 在内部规范化为 `exitVM.*`（getName 实测返回 `exitVM.*`），因此 implies `exitVM.5` 但不 implies `exitVMX`；`a.*` implies `a.b.c` 但不 implies `a`；裸 `"*"` implies 一切；`implies(null)` false；implies 其它 Permission 类型 false；自反恒真。
- **负例构造**：null name → NPE `name can't be null`（精确）；空名 → IAE `name can't be empty`（精确，**空名并不合法**）；双参构造的 actions 被忽略（传任意串 `getActions()` 恒 `""`，null actions 也可）。
- **toString 精确形**：`("java.lang.RuntimePermission" "exitVM")`。
- **序列化**：serialVersionUID 锁 `7399184964622342223`（8/27 相同）；round-trip 保留规范化的 `exitVM.*`。
- **Guard/PermissionCollection**：`guard.checkGuard` 未授予权限时 SecurityException；collection add/implies/setReadOnly 后 add 抛 SecurityException。

### 2. 使用方式

```java
RuntimePermission p = new RuntimePermission("exitVM");
p.implies(new RuntimePermission("exitVM.42")); // true：exitVM 规范化为 exitVM.*
new RuntimePermission("");                      // IllegalArgumentException
```

### 3. 参考来源

- 实测 Temurin jdk-27.0.1.0.2+1（探针 /tmp/lcF）：getName 规范化、异常消息、toString、svuid 全部实测后锁。
- JDK-27 javadoc（RuntimePermission/BasicPermission 路径名规则）。
- 与 JDK-8 侧同探针（行为一致，svuid 相同）。

### 4. 测试用例设计（映射到 RuntimePermissionTest.java 新增方法）+ 易错规范细节

| 测试点 | 用例方法 |
|---|---|
| 6 名称 × getName/getActions、双参忽略 actions | testNamesAndActions |
| null NPE / 空 IAE（精确消息） | testNegativeConstruction |
| exitVM 规范化 + 通配层级 + 自反 + 跨类型 | testImpliesMatrix |
| equals/hashCode 值语义 | testEqualsHashCode |
| toString 精确形 | testToString |
| svuid + round-trip（含规范化保留） | testSerialization |
| Guard + PermissionCollection（readOnly 后 add SE） | testGuardAndCollection |
| final / 父类 / 2 构造器 | testWhiteBox |

- **易错**：**空字符串名称非法**（IAE "name can't be empty"）——不是「空名等于全部」。
- **易错**：`exitVM` 的 getName 已是 `exitVM.*`，断言原字符串会失败。
- **易错**：BasicPermission 的 `getActions()` 恒为空串，双参构造也不保留传入 actions。
