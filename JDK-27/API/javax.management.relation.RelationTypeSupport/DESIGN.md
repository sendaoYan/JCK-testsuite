# javax.management.relation.RelationTypeSupport — 设计案

## 1. API 功能
- 实现 `RelationType`（并 Serializable）。
- 构造器：`RelationTypeSupport(String relationTypeName, RoleInfo[] roleInfoArray)`；`protected RelationTypeSupport(String)`（供子类）。
- 查询：`getRelationTypeName()`、`getRoleInfos()`（返回新的 ArrayList）、`getRoleInfo(String)`。
- `protected addRoleInfo(RoleInfo)` 供子类扩展。

## 2. Java 用户如何使用
典型组合链路：
```java
RelationTypeSupport t = new RelationTypeSupport("myType",
        new RoleInfo[]{ new RoleInfo("r", "java.lang.String") });
String n = t.getRelationTypeName();
List<RoleInfo> roles = t.getRoleInfos();
RoleInfo r = t.getRoleInfo("r");
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/relation/NonArrayListTest.java`：`new RelationTypeSupport("leftRight", new RoleInfo[]{leftRoleInfo, rightRoleInfo})` 构造关系类型后 `addRelationType`（来源：`~/git/jdk25u-dev/test/jdk`）。
- JDK 源码自用：`javax.management.relation.RelationService.createRelationType` 中 `new RelationTypeSupport(relationTypeName, roleInfoArray)`（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation/RelationService.java`）。
- 说明：该类是用户侧创建关系类型的标准实现，无独立开源生态用法。

## 4. 测试用例设计
- 白盒：public 具体类、实现 RelationType + Serializable、公共构造器与查询方法存在。
- 黑盒：构造+getRelationTypeName、`getRoleInfos` 排序后断言、`getRoleInfo` 命中/未命中/null、构造器四类校验（null name/array、空数组、null 元素、重名）的精确异常 message。
