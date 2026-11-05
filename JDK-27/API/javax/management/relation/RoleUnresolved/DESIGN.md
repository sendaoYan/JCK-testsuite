# javax.management.relation.RoleUnresolved — 设计案

## 1. API 功能
- 构造器：`RoleUnresolved(String name, List<ObjectName> value, int pbType)`。
- 查询：`getRoleName()`、`getRoleValue()`（可能为 null）、`getProblemType()`。
- 更新：`setRoleName(String)`（null 抛 IAE）、`setRoleValue(List)`（null 允许，置空）、`setProblemType(int)`（非法状态码抛 IAE）。
- 工具：`clone()`（独立副本）、`toString()`（value 为 null 时省略 `; value:` 段）。

## 2. Java 用户如何使用
典型组合链路：
```java
RoleUnresolved ru = new RoleUnresolved("r", null, RoleStatus.NO_ROLE_WITH_NAME);
int pb = ru.getProblemType();          // 读问题类型
ru.setRoleValue(List.of(ob));          // 设置被拒绝的角色值
String s = ru.toString();              // 诊断展示
```

## 3. 开源代码实际用法调研
- JDK 源码自用：`javax.management.relation.RelationSupport.getRoleInt` 与 `setRoleInt` 在多人访问失败时构造 `new RoleUnresolved(roleName, null, pbType)` / `new RoleUnresolved(roleName, aRole.getRoleValue(), pbType)`（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation/RelationSupport.java`）。
- `javax.management.relation.RoleResult` 的 `setRolesUnresolved` 遍历 `RoleUnresolved` 并 `clone()`（来源：同目录 `RoleResult.java`）。
- 说明：该类为关系服务多角色访问结果的失败条目载体，用户侧多只读 `getProblemType()`，无独立开源生态用法。

## 4. 测试用例设计
- 白盒：public 具体类、实现 Serializable、构造器与全部访问器存在。
- 黑盒：构造+getter、构造值防御性拷贝、三个 setter 组合（含 `setRoleValue(null)` 置空）、带/不带 value 的 `toString` 精确格式、`clone()` 深拷贝、null name 与非法 problem type 的精确 IAE message。

## 深化（2026-11 批 J）
- 断言：25 → 51。新增：problemType 闭域矩阵（1..7 构造与 setter 双通道逐一接受；0/8/-1/MIN/MAX 拒绝且消息精确）、空值 toString 保留 `value: ` 段（`role name: e; value: ; problem type: 7`）、setRoleValue(null) 清空 vs setRoleValue(空列表) 保留非 null、空名合法、null 值克隆保 null（名/类型同拷）、equals/hashCode 身份语义、序列化 round-trip（名/类型/值保序）+ serialVersionUID=-48350262537070138L。
- 负例异常表（27 侧实测）：null 名（构造/setRoleName）→ IAE `Invalid parameter.`；非法 pbType（0/8/-1/MIN/MAX，构造与 setProblemType）→ IAE `Incorrect problem type.`。value=null 合法（读失败标准形态）。
- 版本差异：仅序列化形态机制（JDK-8 双形态/JDK-27 固定）；SUID 一致。
