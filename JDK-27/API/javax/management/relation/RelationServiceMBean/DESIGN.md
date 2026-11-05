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

## 深化（2026-11 批 L：实现闭环 + 接口引用真调用 + server 路由）

### 新增覆盖维度
- **implemented-by 闭环**：接口每个方法（≥30 个）在 RelationService 上 `getMethod(name, params)` 全部可解析（自动全量对照，防接口漂移）。
- **接口引用真流程**：`RelationServiceMBean iface = 注册的服务` → createRelationType/createRelation/hasRelation/getAllRelationIds/getRole/getRelationTypeName/isActive/removeRelation 全真值。
- **server 路由一致性**：`getAttribute(PurgeFlag)`、`getAttribute(AllRelationIds)==iface.getAllRelationIds()`、`setAttribute(PurgeFlag)` 与 `iface.setPurgeFlag` 双向互见、`invoke(getRole)` 与直调等值。
- **MBeanServerInvocationHandler 代理**：`newProxyInstance(server, rsvcName, RelationServiceMBean.class, false)` → getAllRelationIds/getRole 路由真值、观察 removeRelation 生效。

### 易错规范细节（双侧一致）
1. PurgeFlag 是可读写属性（boolean 原生，is-getter 认可）；AllRelationIds/AllRelationTypeNames 为只读属性。
2. 接口方法经代理路由走属性通道（getXxx → getAttribute）与操作通道（带参方法 → invoke）。

