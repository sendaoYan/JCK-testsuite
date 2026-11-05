# javax.management.relation.RelationServiceMBean — 设计案（JDK-8 侧）

## 1. API 功能
RelationService 的管理接口（≥30 方法）。分组与 RelationService 一一对应：
- 属性：PurgeFlag（RW）、AllRelationIds/AllRelationTypeNames（R）。
- 类型管理：createRelationType/addRelationType/removeRelationType/getAllRelationTypeNames。
- 关系管理：createRelation/addRelation/removeRelation/hasRelation/getAllRelationIds/findRelationsOfType/isRelation/isRelationMBean/getRelationTypeName。
- 角色：getRole/getRoles/getAllRoles/setRole/setRoles/getRoleCardinality/retrieveAllRoles(经由 Relation 对象)/checkRoleReading/checkRoleWriting/updateRoleMap。
- 查询：getReferencedMBeans/findReferencingRelations/findAssociatedMBeans。
- 通知与维护：sendRelationCreationNotification/sendRoleUpdateNotification/sendRelationRemovalNotification/purgeRelations/isActive/handleMBeanUnregistration。

两侧行政 API 完全一致（javap 双侧对照无差异）。本侧原测试仅 5-9 条反射断言；2026-11 批 L 追加与 JDK-27 侧逐字相同的深化段（真闭环行为测试）。

## 2. Java 用户如何使用
本地 `MBeanServerFactory.newMBeanServer()` + `new RelationService(true)` 注册到 `jck.rsvc:type=RelationService`，
自造标准 MBean（Counter/Text）作角色成员，`createRelationType` → `createRelation` → 角色/查询/通知/purge 全链路。

## 3. 参考来源
- Oracle javadoc：javax.management.relation 包文档（Relation Service 规格、RoleInfo 度约束、purge 语义、RelationNotification 六类型）。
- OpenJDK 源码实测：`~/git/jdk8u-dev/src/share/classes/javax/management/relation/`（RelationService/RelationSupport/RoleInfo/RelationNotification），消息文案以源码+双侧探针实测为准。
- OpenJDK 回归思路：`~/git/jdk8u-dev/test/jdk/javax/management/relation/`（RelationTypeTest/NonArrayListTest 等，只借鉴思路）。

## 4. 测试用例设计
原反射断言保留；追加深化段与 JDK-27 侧完全相同，详见下方「深化」节。

## 深化（2026-11 批 L：实现闭环 + 接口引用真调用 + server 路由）

### 新增覆盖维度
- **implemented-by 闭环**：接口每个方法（≥30 个）在 RelationService 上 `getMethod(name, params)` 全部可解析（自动全量对照，防接口漂移）。
- **接口引用真流程**：`RelationServiceMBean iface = 注册的服务` → createRelationType/createRelation/hasRelation/getAllRelationIds/getRole/getRelationTypeName/isActive/removeRelation 全真值。
- **server 路由一致性**：`getAttribute(PurgeFlag)`、`getAttribute(AllRelationIds)==iface.getAllRelationIds()`、`setAttribute(PurgeFlag)` 与 `iface.setPurgeFlag` 双向互见、`invoke(getRole)` 与直调等值。
- **MBeanServerInvocationHandler 代理**：`newProxyInstance(server, rsvcName, RelationServiceMBean.class, false)` → getAllRelationIds/getRole 路由真值、观察 removeRelation 生效。

### 易错规范细节（双侧一致）
1. PurgeFlag 是可读写属性（boolean 原生，is-getter 认可）；AllRelationIds/AllRelationTypeNames 为只读属性。
2. 接口方法经代理路由走属性通道（getXxx → getAttribute）与操作通道（带参方法 → invoke）。

