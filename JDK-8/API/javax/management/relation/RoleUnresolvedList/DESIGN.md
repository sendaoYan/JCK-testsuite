# javax.management.relation.RoleUnresolvedList — 设计案（JDK-8）

## 1. API 功能
- `RoleUnresolvedList extends ArrayList<Object>`：构造 `()`、`(int)`、`(List<RoleUnresolved>)`（含非 RoleUnresolved 元素抛 IAE；null 抛 IAE `Null parameter`）。
- 类型安全视图：`asList()` → `List<RoleUnresolved>`（同一实例强转视图，双向活引用）。
- 元素操作：`add(RoleUnresolved)`/`add(int, RoleUnresolved)`/`set(int, RoleUnresolved)`（null 抛 IAE）、`addAll(RoleUnresolvedList)`（null 返回 true）/`addAll(int, RoleUnresolvedList)`（null 抛 IAE）；覆写 ArrayList 层 `add(Object)`/`addAll(Collection)` 做元素类型检查。
- 注意：RoleList 里的 Role 在这里同样被拒（两类列表各自严格限定元素类型）。

## 2. Java 用户典型用法
```java
RoleUnresolvedList failures = new RoleUnresolvedList();
failures.add(new RoleUnresolved("owner", null, RoleStatus.NO_ROLE_WITH_NAME));
RoleResult result = new RoleResult(successfulRoles, failures);
failures.asList();    // List<RoleUnresolved> 视图
```

## 3. 参考来源
- Oracle javadoc：javax.management.relation.RoleUnresolvedList（asList 契约、addAll(null) 返回 true）。
- OpenJDK 源码 `~/git/jdk8u-dev/.../relation/RoleUnresolvedList.java`（与 RoleList 平行的实现；**消息字面量与 RoleList 略有出入**，见易错点）。
- 行为以 jdk8u372-b07 二进制实测为准。

## 4. 测试用例设计 + 易错规范细节
- 断言：JDK-8 侧 3 → 63。
- 维度：3 构造形态 + 容量负值、非 RoleUnresolved 构造 IAE（含"放进 Role 也不行"）、add/set/get 与 IOOBE 矩阵、null 元素消息、addAll 全形态（null 语义）、**JDK-8 遗留非类型化访问**、迭代/remove/contains/toArray/clear、asList 活视图、序列化 + SUID + 继承 clone。
- **易错点 1（本批核心版本差异，实测）**：与 RoleList 相同——JDK-8 在首次 `asList()` 前 raw add/addAll 非 RoleUnresolved 被容忍（taint），之后 `asList()` 抛 IAE（包 CCE）；`asList()` 后 raw add 立即抛。JDK-27 任何时候都立即抛。
- **易错点 2（实测，与 RoleList 的真实差异）**：本类所有 IAE 消息均为 `Invalid parameter`（**无句点**）——包括 `set(0,null)` 与 `addAll(0,null)`；而 RoleList 的 set/addAll(int,null) 消息是 `Invalid parameter.`（带句点）。JDK 源码两处字面量不一致，断言按各自实测锁定。
- **易错点 3**：`add((Object) null)` 在类型安全模式下也被接受（null 通过强转）；`addAll(null)` 返回 true（文档化特例）；容量负 → `Illegal Capacity: -1`；越界 → IndexOutOfBoundsException。
- 序列化：serialVersionUID = 4054902803091433324L；继承 clone() 保持 RoleUnresolvedList 运行时类型（浅拷贝）。
- **版本差异（vs JDK-27）**：JDK-8 有 typeSafe/tainted 遗留兼容层（见易错点 1），JDK-27 移除；CCE 消息格式不同（27 带模块信息）；SUID 一致。
