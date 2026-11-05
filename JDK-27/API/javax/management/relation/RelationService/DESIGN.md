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

## 深化（2026-11 批 L：RelationService 真闭环）

### 新增覆盖维度（本地 MBeanServer 真闭环，非反射冒烟）
- **createRelation 闭环**：注册 `RelationService(true)` 到 `jck.rsvc:type=RelationService` + 两个自造标准 MBean（Counter A/B/C + 用于类型不匹配的 Text），`createRelationType("jck.T", RoleInfo("memberRole", Counter, 1..2))` → `createRelation` → 逐 getter 精确断言（getAllRelationIds/getAllRelationTypeNames/getRelationTypeName/getRole/getRoleCardinality/getAllRoles/getReferencedMBeans A→["memberRole"]/findRelationsOfType/findReferencingRelations（含 type/role 过滤正反例）/findAssociatedMBeans/isRelationMBean null/isRelation null）。
- **角色约束真触发（精确异常+消息）**：
  - `getRole("r1","bogusRole")` → `RoleNotFoundException("bogusRole does not exist in relation.")`
  - 低于 minDegree（空列表）→ `InvalidRoleValueException("memberRole has a number of MBean references less than the expected minimum degree.")`
  - 超 maxDegree（3 个已注册成员，max=2）→ `InvalidRoleValueException("...greater than the expected maximum degree.")`
  - 成员未注册 → `InvalidRoleValueException("memberRole has a reference to null or to an MBean not registered.")`
  - 类型不匹配（Text MBean 塞进 Counter 角色）→ `InvalidRoleValueException("...has an MBean reference to an MBean not of the expected class of references for that role.")`
  - 重复 relation id → `InvalidRelationIdException("There is already a relation with id r1")`；未知类型 → `RelationTypeNotFoundException("No relation type created in the Relation Service with the name jck.Nope")`
  - 读写权限：不可读 → `RoleNotFoundException("silentRole is not readable.")`、不可写 → `("silentRole is not writable.")`
- **多角色 RoleResult**：`getRoles` 成功+失败混（bogusRole 进 RoleUnresolved，problemType==`RoleStatus.NO_ROLE_WITH_NAME`，value==null）；`setRoles` 混合（未解析项保留尝试值）；setRoles 内低于 min 的角色以 LESS_THAN_MIN_ROLE_DEGREE 进未解析列表且不应用。
- **RoleStatus 常量**：NO_ROLE_WITH_NAME=1、ROLE_NOT_READABLE=2、ROLE_NOT_WRITABLE=3、LESS_THAN_MIN_ROLE_DEGREE=4、MORE_THAN_MAX_ROLE_DEGREE=5、REF_MBEAN_OF_INCORRECT_CLASS=6、REF_MBEAN_NOT_REGISTERED=7；`isRoleStatus` 边界；`checkRoleReading` 返回 2/1、`checkRoleWriting` 返回 3。
- **purge 语义**：immediatePurge=true 注销成员 B → getRole 立即剔除且发 update.basic 通知（old [A,B]→new [A]）；继续注销 A（跌破 min=1）→ 整个关系被移除 + removal.basic 通知；immediatePurge=false → 注销后 getRole 仍返回陈旧值，`purgeRelations()` 后才剔除。
- **通知闭环**：source 被 MBeanServer 改写为服务的 ObjectName；creation.basic 消息 "Creation of relation r1"；update.basic 消息 `"Value of role X has changed\nOld value:\n<A>\n<B>\nNew value:\n<C>"`（`Role.roleValueToString` 用 **换行** 连接、无方括号）；removal.basic 消息 "Removal of relation r1"；seqNumber 严格递增；手动 `sendRelationCreationNotification/sendRoleUpdateNotification/sendRelationRemovalNotification`（未知 id → RelationNotFoundException）。
- **addRelation 外部 Relation**：自造 `ExtRelMBean extends Relation` 记录型实现注册为 MBean 后 `addRelation` → 发 **RELATION_MBEAN_CREATION**、isRelationMBean/isRelation 双向、getRole/getReferencedMBeans/setRole 全部真路由到用户实现（calls 记录验证）；removeRelation 后 MBean 本身仍注册；非 Relation MBean → `NoSuchMethodException("This MBean does not implement the Relation interface.")`；未注册名 → `InstanceNotFoundException`。
- **RelationSupport 作关系 MBean**：注册 + addRelation → isInRelationService 翻 true、creation/update 通知走 **mbean** 变体（getObjectName==RelationSupport 的 ObjectName）；4 参构造经 preRegister 捕获 server 同样可用。
- **removeRelationType 连带删关系**：hasRelation false、后续 createRelation 该类型 → RelationTypeNotFoundException；`updateRoleMap` 只搬服务侧引用表（findReferencingRelations 反映、关系值不动）。
- **未注册服务**：createRelation/removeRelation/purgeRelations → RelationServiceNotRegisteredException("Relation Service not registered in the MBean Server.")。

### 易错规范细节（实测锁定，JDK-8 与 JDK-27 完全一致）
1. `createRelation` 会自动发 creation.basic 通知（addRelationInt 末尾调用 sendRelationCreationNotification）；消息文案固定。
2. **null RoleList**：min>0 → IRVE（缺省初始化失败）；min==0 → **NullPointerException**（addRelationInt 中 `roleList.asList()` 未判空，双侧行为一致的记录性缺陷）；空 RoleList + min==0 → 角色默认空值、合法。
3. 注销成员（immediate purge）会经 RelationSupport.handleMBeanUnregistration 发 update.basic 通知。
4. removeRelationType 移除该类型的全部关系（连带 removal 通知）。
5. **版本差异**：仅 NPE 消息文案（JDK-27 有 helpful message，JDK-8 为 null）——测试只断言异常类型。

