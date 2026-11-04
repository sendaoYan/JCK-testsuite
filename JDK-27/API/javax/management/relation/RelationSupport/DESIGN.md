# javax.management.relation.RelationSupport — 设计案

## 1. API 功能
- 具体类，实现 `RelationSupportMBean`（进而 `Relation`）与 `MBeanRegistration`。
- 构造器：
  - `RelationSupport(String relationId, ObjectName relationServiceName, String relationTypeName, RoleList list)`；
  - `RelationSupport(String, ObjectName, MBeanServer, String, RoleList)`（额外要求 MBeanServer）。
- 查询：`getRelationId`/`getRelationTypeName`/`getRelationServiceName`/`getRole`/`getRoles`/`getAllRoles`/`retrieveAllRoles`/`getRoleCardinality`/`getReferencedMBeans`/`isInRelationService`。
- 更新：`setRole`/`setRoles`/`setRelationServiceManagementFlag`/`handleMBeanUnregistration`。
- MBean 生命周期：`preRegister`/`postRegister`/`preDeregister`/`postDeregister`。

## 2. Java 用户如何使用
典型组合链路：
```java
RelationSupport r = new RelationSupport("id", relSvcName, "type", roleList);
String id = r.getRelationId();
int n = r.getRoleCardinality("role");
Map<ObjectName,List<String>> refs = r.getReferencedMBeans();
r.setRelationServiceManagementFlag(Boolean.TRUE);
```
本测试聚焦**不依赖真实 MBeanServer** 的确定性部分。

## 3. 开源代码实际用法调研
- JDK 源码自用：`RelationService.createRelation` 内部 `new RelationSupport(relationId, ...)` 构造内部关系；`RelationSupport implements RelationSupportMBean, MBeanRegistration`（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation`）。
- JDK `test/jdk/javax/management/relation/NonArrayListTest.java` 通过 `RelationService` 间接创建 `RelationSupport` 内部关系。
- 说明：用户也可直接继承/组合 `RelationSupport` 构建自定义关系 MBean。

## 4. 测试用例设计
- 白盒：public 具体类、实现 RelationSupportMBean + Relation、两构造器与关键方法存在。
- 黑盒（无 MBeanServer）：构造器校验（null id/serviceName/typeName/MBeanServer、重复角色名）、`getRelationId`/`getRelationTypeName`/`getRelationServiceName`、`retrieveAllRoles` 排序断言、`getRoleCardinality`、`getReferencedMBeans`、`setRelationServiceManagementFlag`、`getRole(null/unknown)`、`getRoles(null)`、`getRoleCardinality(null/unknown)`、`setRole(null)`、`setRoles(null)`、空关系 `getAllRoles`。
