# javax.management.relation.RoleUnresolved — 设计案（JDK-8）

## 1. API 功能
- 构造器：`RoleUnresolved(String name, List<ObjectName> value, int pbType)`——null 名抛 IAE；**value 可为 null**（读访问失败时不带值）；pbType 必须是 RoleStatus 常量（1..7），否则 IAE `Incorrect problem type.`。
- 查询：`getRoleName()`、`getRoleValue()`（null 或防御性拷贝的 ArrayList）、`getProblemType()`。
- 更新：`setRoleName(String)`（null 抛 IAE）、`setRoleValue(List)`（null 合法——显式清空）、`setProblemType(int)`（域校验同构造器）。
- 工具：`toString()`（有值 `role name: X; value: a, b; problem type: N`；null 值省略 value 段）、`clone()`（经构造器重建）。

## 2. Java 用户典型用法
```java
// RelationService.getRoles 返回 RoleResult，读失败的角色进 RoleUnresolvedList
RoleUnresolved bad = new RoleUnresolved("owner",
        Arrays.asList(unregisteredName),
        RoleStatus.REF_MBEAN_NOT_REGISTERED);
RoleResult rr = new RoleResult(okRoles, new RoleUnresolvedList(Arrays.asList(bad)));
```

## 3. 参考来源
- Oracle javadoc：javax.management.relation.RoleUnresolved（problemType 与 RoleStatus 常量的对应、读失败 value 为 null 的语义）。
- OpenJDK 源码 `~/git/jdk8u-dev/.../relation/RoleUnresolved.java`（`RoleStatus.isRoleStatus(pbType)` 委托校验、clone 经构造器、toString 分段拼接）。
- 行为以 jdk8u372-b07 二进制实测为准。

## 4. 测试用例设计 + 易错规范细节
- 断言：JDK-8 侧 4 → 59。
- 维度：problemType 全域矩阵（1..7 逐一接受 + 0/8/-1/MIN/MAX 拒绝且消息精确，构造与 setter 双通道）、null/空名/空值/防御性拷贝、setter 全语义（换值/拷贝/清空/空列表）、toString 三形态（有值/null 值省略段/空值保留段）、clone（值独立、null 值克隆保 null）、equals/hashCode 身份语义、序列化 round-trip（保序+类型）+ SUID。
- **易错点 1（消息）**：null 名（构造与 setRoleName）→ `Invalid parameter.`（带句点，与 Role 构造器的无句点消息不同）；非法 pbType → `Incorrect problem type.`。
- **易错点 2**：value 语义与 Role 相反——Role 的 null 值非法，RoleUnresolved 的 null 值合法且是读失败的标准形态；`setRoleValue(null)` 是显式清空。
- **易错点 3（实测）**：pbType 域恰为 RoleStatus 的 7 个常量（闭域）；toString 对**空列表**值保留 `value: ` 段（`role name: e; value: ; problem type: 7`），仅 null 值才省略。
- **易错点 4**：不覆写 equals/hashCode（身份语义）；clone 后值列表独立（构造器重拷贝）。
- 序列化：serialVersionUID = -48350262537070138L。
- **版本差异（vs JDK-27）**：仅 JDK-8 有双 serial form（jmx.serial.form=1.0 旧形态）；默认形态 SUID 一致，行为/消息逐字节一致。
