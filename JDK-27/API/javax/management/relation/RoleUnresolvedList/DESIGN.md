# javax.management.relation.RoleUnresolvedList — 设计案

## 1. API 功能
- 继承 `ArrayList<Object>`（与 RoleList 同款历史原因）。
- 构造器：`()`、`(int initialCapacity)`、`(List<RoleUnresolved>)`。
- 类型安全操作：`add`/`add(int)`/`set`/`addAll`/`addAll(int)` 的 RoleUnresolved 重载。
- `asList()` 返回 `List<RoleUnresolved>` 实时视图，并开启严格类型检查。

## 2. Java 用户如何使用
典型组合链路：
```java
RoleUnresolvedList r = new RoleUnresolvedList();
r.add(new RoleUnresolved("x", null, RoleStatus.NO_ROLE_WITH_NAME));
for (RoleUnresolved u : r.asList()) { ... }
```

## 3. 开源代码实际用法调研
- JDK 源码自用：`javax.management.relation.RelationSupport.getRolesInt` / `setRolesInt` 中 `new RoleUnresolvedList()` 并 `add((RoleUnresolved)currResult)`（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation/RelationSupport.java`）。
- `javax.management.relation.RoleResult.setRolesUnresolved` 中 `new RoleUnresolvedList()`（来源：同目录 `RoleResult.java`）。
- 说明：该类是关系服务批量操作失败条目的容器，与 RoleList 结构对称，无独立开源生态用法。

## 4. 测试用例设计
- 白盒：public 类、父类 ArrayList、三构造器与类型安全方法存在。
- 黑盒：add/insert/set/addAll 组合与插入顺序保持、`asList()` 实时视图与类型安全开关、构造器拒绝非 RoleUnresolved、null 参数精确 message、`addAll(null)` 文档化返回 true。
