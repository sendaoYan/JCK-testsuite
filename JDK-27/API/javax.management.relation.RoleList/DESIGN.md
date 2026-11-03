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
