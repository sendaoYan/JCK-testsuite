# javax.management.relation.RelationType — 设计案

## 1. API 功能
- 接口，继承 `Serializable`。
- 查询：`getRelationTypeName()`、`getRoleInfos()`（`List<RoleInfo>`）、`getRoleInfo(String)`。

## 2. Java 用户如何使用
关系类型对象向关系服务声明角色结构；用户通过实现类 `RelationTypeSupport` 提供实现。

## 3. 开源代码实际用法调研
- JDK 源码自用：`RelationService.addRelationType(RelationType)` 读取 `relationTypeObj.getRoleInfos()`；`RelationTypeSupport implements RelationType`（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation`）。
- JDK `test/jdk/javax/management/relation/NonArrayListTest.java`：`new RelationTypeSupport(...)` 后 `rsProxy.addRelationType(leftRightType)`。

## 4. 测试用例设计
- 白盒：public 接口、继承 Serializable、三个查询方法存在、`getRoleInfos` 返回 List。
- 轻行为：`RelationTypeSupport` 实现该接口（实现类链接验证）。
