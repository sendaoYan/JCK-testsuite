# javax.management.relation.RoleList — 设计案

## 1. API 功能
- 继承 `ArrayList<Object>`（因历史遗留方法签名无法继承 `ArrayList<Role>`）。
- 构造器：`()`、`(int initialCapacity)`、`(List<Role>)`。
- 类型安全操作：`add(Role)`、`add(int, Role)`、`set(int, Role)`、`addAll(RoleList)`、`addAll(int, RoleList)`。
- `asList()` 返回 `List<Role>` 实时视图，并开启此后对原始 `add(Object)`/`set`/`addAll` 的严格类型检查。

## 2. Java 用户如何使用
典型组合链路：
```java
RoleList list = new RoleList();
list.add(new Role("r", values));       // 追加
list.add(0, new Role("s", values));    // 插入
list.set(1, new Role("t", values));    // 替换
list.addAll(anotherRoleList);          // 合并
for (Role r : list.asList()) { ... }   // 类型安全迭代
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/relation/RelationTypeTest.java`：`new RoleList(Arrays.asList(new Role[]{leftRole, rightRole}))` 构造关系初始角色集。
- JDK `test/jdk/javax/management/relation/NonArrayListTest.java`：`new RoleList(new ArrayList(Arrays.asList(new Role[]{leftRole, rightRole})))`。
- JDK 源码自用：`javax.management.relation.RelationSupport.retrieveAllRoles` 中 `new RoleList(new ArrayList<>(...))`，`setRolesInt` 中遍历 `list.asList()`（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation/RelationSupport.java`）。

## 4. 测试用例设计
- 白盒：public 类、父类为 ArrayList、三构造器与类型安全方法存在。
- 黑盒：add/insert/set/addAll 组合与插入顺序保持、`asList()` 实时视图与类型安全开关、`List<Role>` 构造器拒绝非 Role 元素、五处 null 参数的精确 IAE message、`addAll(null)` 的文档化返回 true。

## 深化（2026-11 批 J）
- 断言：28 → 43。新增：**JDK-27 严格类型语义**（raw `add(非Role)`/`addAll(含非Role集合)` 在 `asList()` 调用**之前**也立即抛 IAE（cause=ClassCastException）——与 JDK-8 的 taint 容忍相反，本版已删除 typeSafe/tainted 兼容层）、`add((Object)null)` 类型安全模式下仍被接受（null 通过强转）、容量负 → IAE `Illegal Capacity: -1`、add/get/set 越界 IOOBE 矩阵、ArrayList 层 raw `set` 返回旧元素、iterator.remove/contains/toArray、序列化 round-trip + serialVersionUID=5568344346499649313L、继承 clone() 保持 RoleList 运行时类型（浅拷贝、元素共享）。
- 负例异常表（27 侧实测）：ctor(null) → IAE `Null parameter`；ctor(非Role) / raw add(非Role) / addAll(非Role) → IAE（包 CCE，消息含模块信息，与 JDK-8 格式不同）；add(null)/add(0,null) → IAE `Invalid parameter`；set(0,null)/addAll(0,null) → IAE `Invalid parameter.`；ctor(-1) → IAE `Illegal Capacity: -1`；越界 → IndexOutOfBoundsException。
- 版本差异（核心）：JDK-8 在首次 asList() 前容忍 raw 非 Role 添加（taint 后 asList() 抛 IAE）；JDK-27 恒拒绝。SUID 一致。
