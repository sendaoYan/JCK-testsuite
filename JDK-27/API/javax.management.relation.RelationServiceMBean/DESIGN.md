# javax.management.relation.RelationServiceMBean — 设计案

## 1. API 功能
- 接口：关系服务的标准 MBean 管理接口。
- 生命周期与配置：`isActive`、`getPurgeFlag`/`setPurgeFlag`。
- 关系类型管理：`createRelationType`/`addRelationType`/`getAllRelationTypeNames`/`getRoleInfos`/`getRoleInfo`/`removeRelationType`。
- 关系管理：`createRelation`/`addRelation`/`isRelationMBean`/`isRelation`/`hasRelation`/`getAllRelationIds`/`removeRelation`/`purgeRelations`。
- 查询与更新：`checkRoleReading`/`checkRoleWriting`/`findReferencingRelations`/`findAssociatedMBeans`/`findRelationsOfType`/`getRole(s)`/`getAllRoles`/`getRoleCardinality`/`setRole(s)`/`getReferencedMBeans`/`getRelationTypeName`。

## 2. Java 用户如何使用
用户通过 `JMX.newMBeanProxy(mbs, name, RelationServiceMBean.class)` 获得代理后调用上述方法（见 `RelationNotificationSeqNoTest`）。

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/relation/RelationNotificationSeqNoTest.java`：`JMX.newMBeanProxy(mbs, relSvcName, RelationServiceMBean.class)`（来源：`~/git/jdk25u-dev/test/jdk`）。
- JDK `test/jdk/javax/management/relation/NonArrayListTest.java`：`MBeanServerInvocationHandler.newProxyInstance(..., RelationServiceMBean.class, false)`。
- JDK 源码自用：`RelationService implements RelationServiceMBean`。

## 4. 测试用例设计
- 白盒：public 接口、约 28 个管理方法签名存在。
- 轻行为：`RelationService` 实现该接口（实现类链接验证）。
