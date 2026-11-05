# javax.management.relation.RoleList — 设计案（JDK-8）

## 1. API 功能
- `RoleList extends ArrayList<Object>`：构造 `()`、`(int initialCapacity)`、`(List<Role>)`（含非 Role 元素抛 IAE；null 抛 IAE `Null parameter`）。
- 类型安全视图：`asList()` → `List<Role>`（同一实例的强转视图，双向活引用）。
- 元素操作：`add(Role)`/`add(int, Role)`/`set(int, Role)`（null 抛 IAE）、`addAll(RoleList)`（null 参数**返回 true** 不抛）/`addAll(int, RoleList)`（null 抛 IAE）；继承 ArrayList 的 `add(Object)`/`addAll(Collection)` 等被覆写做类型检查。
- 查询/迭代：`get/iterator/listIterator/contains/indexOf/toArray/remove/clear` 全走 ArrayList 契约；继承的 `clone()` 保持 RoleList 运行时类型（浅拷贝）。

## 2. Java 用户典型用法
```java
RoleList roles = new RoleList();
roles.add(new Role("owner", Arrays.asList(ownerName)));
roles.asList();                       // List<Role> 视图（此后列表进入类型安全模式）
RoleResult rr = new RoleResult(roles, null);
```

## 3. 参考来源
- Oracle javadoc：javax.management.relation.RoleList（asList 视图契约、addAll(null) 返回 true 的文档化行为）。
- OpenJDK 源码 `~/git/jdk8u-dev/.../relation/RoleList.java`（typeSafe/tainted 双 transient 标志——JDK-8 独有的遗留兼容机制；checkTypeSafe 用 `(Role) o` 强转并包 CCE 为 IAE）。
- OpenJDK 回归思路：`~/git/jdk/test/jdk/javax/management/relation/RelationNotificationTest.java` 等（RoleList 传参用法）。
- 行为以 jdk8u372-b07 二进制实测为准。

## 4. 测试用例设计 + 易错规范细节
- 断言：JDK-8 侧 3 → 65。
- 维度：3 构造形态 + 容量负值、非 Role 构造 IAE（CCE 包装 + cause 断言）、add/set/get 与 IOOBE 越界矩阵、null 元素消息（`Invalid parameter` add 用 / `Invalid parameter.` set 用——注意句点差异）、addAll 全形态（含 null 语义）、**JDK-8 遗留非类型化访问**、迭代/remove/contains/indexOf/toArray/clear、asList 活视图双向、序列化 round-trip + SUID + 继承 clone 类型。
- **易错点 1（本批核心版本差异，实测）**：JDK-8 在 `asList()` 首次调用**之前**，raw `add("字符串")`/`addAll(含非 Role 集合)` 被**容忍**（置 tainted 标志，元素真的进列表）；此后调用 `asList()` 抛 IAE（包 CCE）。`asList()` 调用过一次后（typeSafe=true），raw add 非 Role 立即抛 IAE。JDK-27 无此兼容层，任何时刻 raw add 非 Role 都立即抛 IAE（见 27 侧 DESIGN）。
- **易错点 2（实测）**：`add((Object) null)` 在类型安全模式下也**被接受**——null 能通过内部 `(Role) o` 强转；而 `add((Role) null)` 抛 IAE（null 检查在前）。`listIterator().add(非Role)` 经由覆写的 `add(int,Object)` 同样抛 IAE。
- **易错点 3（消息）**：构造 `Null parameter`；`add(null)`/`add(0,null)` → `Invalid parameter`（无句点）；`set(0,null)`/`addAll(0,null)` → `Invalid parameter.`（带句点）；`addAll(RoleList null)` 返回 true（文档化特例）。容量负 → ArrayList 的 `Illegal Capacity: -1`。
- **易错点 4**：越界（add/get/set/addAll(index)）→ IndexOutOfBoundsException；CCE 包装的 IAE 消息两侧格式不同（JDK-27 带模块信息），断言用 contains("cannot be cast to")。
- 序列化：serialVersionUID = 5568344346499649313L；typeSafe/tainted 是 transient（反序列化后重置）。
- **版本差异（vs JDK-27）**：见易错点 1 —— JDK-8 有 typeSafe/tainted 遗留兼容，JDK-27 删除并收紧为恒检查；SUID 一致。
