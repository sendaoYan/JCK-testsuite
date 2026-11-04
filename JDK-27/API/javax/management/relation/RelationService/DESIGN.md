# javax.management.relation.RelationService — 设计案

## 1. API 功能
- 具体类，实现 `RelationServiceMBean`、`MBeanRegistration`、`NotificationListener`，继承 `NotificationBroadcasterSupport`。
- 构造器：`RelationService(boolean immediatePurgeFlag)`。
- 生命周期：`isActive()`（未注册到 MBeanServer 时抛 `RelationServiceNotRegisteredException`）、`preRegister`/`postRegister`/`preDeregister`/`postDeregister`。
- 关系类型管理：`createRelationType`/`addRelationType`/`getAllRelationTypeNames`/`getRoleInfos`/`getRoleInfo`/`removeRelationType`。
- 关系管理：`createRelation`/`addRelation`/`isRelationMBean`/`isRelation`/`hasRelation`/`getAllRelationIds`/`removeRelation`/`purgeRelations`。
- 查询：`checkRoleReading`/`checkRoleWriting`/`findRelationsOfType`/`findReferencingRelations`/`findAssociatedMBeans`/`getRole(s)`/`getAllRoles`/`getRoleCardinality`/`setRole(s)`/`getReferencedMBeans`/`getRelationTypeName`/`getNotificationInfo`。

## 2. Java 用户如何使用
典型组合链路（先注册到 MBeanServer，再创建关系类型与关系）：
```java
RelationService rs = new RelationService(true);
mbs.registerMBean(rs, relSvcName);
rs.createRelationType("type", new RoleInfo[]{...});
rs.createRelation("relId", "type", roleList);
rs.checkRoleReading("role", "type");
rs.getAllRelationIds();
```
本测试聚焦**未注册**服务的确定性默认状态与类型管理（不真正注册到平台 MBeanServer）。

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/relation/RelationTypeTest.java`：通过 `MBeanServerFactory.newMBeanServer()` 注册 `RelationService`，再 `createRelationType`/`createRelation`（来源：`~/git/jdk25u-dev/test/jdk`）。
- JDK `test/jdk/javax/management/relation/NonArrayListTest.java`：`new RelationService(true)` + `mbs.registerMBean(rs, rsName)` + `addRelationType`/`createRelation`。
- JDK 源码自用：`RelationService` 是 JMX 关系服务核心实现（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation/RelationService.java`）。

## 4. 测试用例设计
- 白盒：public 具体类、实现三个接口、关键方法存在。
- 黑盒（不注册，不碰全局 MBeanServer）：构造器 purge 标志、`isActive` 未注册异常、空注册表、null 参数校验、`createRelationType`→`getAllRelationTypeNames`→`getRoleInfos`→`getRoleInfo` 组合、`checkRoleReading` 返回 0/NO_ROLE_WITH_NAME、`checkRoleWriting` 基数不足返回 LESS_THAN_MIN、`findRelationsOfType` 空结果、`removeRelationType` 未注册异常、`getNotificationInfo` 单条 6 类型。
