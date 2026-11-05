# javax.management.relation.RelationNotification — 设计案（JDK-8 侧）

## 1. API 功能
Relation Service 发出的通知（继承 `Notification`，`Serializable`）。分组：
- **常量**：六类型串 jmx.relation.creation.basic / removal.basic / update.basic / creation.mbean / removal.mbean / update.mbean。
- **构造**：6 参（creation/removal 形态：type, source, seq, timeStamp, message, id, typeName, unregMBeanList 实为 8 个有效参）与 11 参（update 形态，多 roleName/oldValue/newValue/objectName）；非法组合抛 IllegalArgumentException("Invalid parameter.")。
- **getter**：getRelationId/getRelationTypeName/getObjectName/getMBeansToUnregister/getRoleName/getOldRoleValue/getNewRoleValue。
- **序列化**：serialVersionUID = -6871117877523310399L。

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

## 深化（2026-11 批 L：构造形态矩阵 + 序列化 + 真闭环）

### 新增覆盖维度
- **6 参构造（creation/removal 形态）**：RELATION_BASIC_REMOVAL / RELATION_MBEAN_CREATION / RELATION_MBEAN_REMOVAL 三种合法类型逐一构造（此前只测过 basic creation）；roleName null、old/new 空列表、objectName null 或精确回读、null unregMBeanList 归一化为空列表。
- **11 参构造（update 形态）**：RELATION_MBEAN_UPDATE 合法构造、空 old 列表归一化、objectName 精确、source 引用在 MBeanServer 之外**不被改写**（getSource()==原对象）。
- **IAE 矩阵（精确消息）**：null source → `"null source"`（来自 Notification 父类）；null id / null typeName / 非法 type / 6 参传 update 类型 / 11 参传 creation 类型 / null roleName / null newValue / null oldValue → `"Invalid parameter."`。
- **序列化**：两种形态 round-trip 全 getter 精确（type/relationId/typeName/objectName/unregister 列表/seq/timeStamp/message/source/roleName/old/new）；`serialVersionUID == -6871117877523310399L`（ObjectStreamClass 锁定）。
- **真闭环**：注册 RelationService(true) + 监听 → createRelation/setRole/removeRelation 收到 real RelationNotification，逐 getter（relationId/typeName/source==服务 ObjectName/roleName/old/new/removal 消息）。

### 易错规范细节（双侧一致）
1. `getMBeansToUnregister()`：非 null 入参返回防御拷贝；**null 入参归一化为不可变 `Collections.emptyList()`**（add → UnsupportedOperationException，测试按记录行为兼容两种实现）。
2. creation/removal 形态的 getRoleName()==null、getOld/NewRoleValue() 为空列表（构造器 safeGet 归一化）。
3. 通知经 MBeanServer 投递后 source 被改写为服务的 ObjectName；不经 server 直接构造则保留原引用。

