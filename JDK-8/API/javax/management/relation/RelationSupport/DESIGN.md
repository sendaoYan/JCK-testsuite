# javax.management.relation.RelationSupport — 设计案（JDK-8 侧）

## 1. API 功能
内建关系对象（实现 `RelationSupportMBean`+`Relation`+`MBeanRegistration`）。分组：
- **构造**：4 参（id, serviceName, serviceServerName, type）建后 setRole 填值；5 参（+ RoleList）一步到位。
- **Relation 契约**：getRole/getRoles/getAllRoles/retrieveAllRoles/getRoleCardinality/setRole/setRoles/handleMBeanUnregistration/getReferencedMBeans/getRelationTypeName/getRelationServiceName/getRelationId。
- **自有**：isInRelationService()/setRelationServiceManagementFlag(Boolean)。
- **MBeanRegistration**：preRegister 捕获 MBeanServer、postRegister、preDeregister、postDeregister——使其可直接 registerMBean 后由 addRelation 纳管。

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

## 深化（2026-11 批 L：脱离/挂靠服务两态 + MBean 管理面）

### 新增覆盖维度
- **完全脱离服务（4 参构造、未注册）**：`getRoleCardinality`/`retrieveAllRoles`/`getReferencedMBeans` 纯本地可用；`getRole` 未知角色 → RoleNotFoundException("nope does not exist in relation.")；已知角色读取需经 MBeanServer 调 checkRoleReading → 无 server 时 **NPE**（记录）；`getRoles(["nope"])` 返回 RoleUnresolved（NO_ROLE_WITH_NAME）不触服务；`getAllRoles` 同样 NPE。
- **5 参构造 + 幽灵服务名**：getRole/setRole → `RelationServiceNotRegisteredException`。
- **5 参构造 + 真服务**：getRole 可用（读路径**不校验**成员注册性——幽灵成员仍可读）；setRole 约束先于路由生效（低于 min → IRVE 精确消息、类型不匹配 → IRVE、未知角色名 → RoleNotFoundException）；值合法但未 addRelation → `RelationNotFoundException("No relation associated to relation id sup3")`（sendRoleUpdateNotification 阶段失败）。
- **注册为 MBean（4 参构造经 preRegister 捕获 server）**：精确管理面——`MBeanInfo` 属性恰为 7 个（AllRoles/ReferencedMBeans/RelationId/RelationServiceManagementFlag/RelationServiceName/RelationTypeName/Role），其中 Role 与 RelationServiceManagementFlag 为**只写**属性；`InRelationService` **不是属性**（isInRelationService 返回 Boolean 而非原生 boolean，JMX 内省不认）→ getAttribute 抛 AttributeNotFoundException；setAttribute flag 真生效；经 server 传 null flag → `RuntimeMBeanException` 包装 `IllegalArgumentException("Invalid parameter.")`；invoke retrieveAllRoles/getRoleCardinality 路由真值；addRelation 后经服务 setRoles 双向生效；`Relation` 接口引用透传断言。

### 易错规范细节（双侧一致）
1. RelationSupport 直接 new 出来（未注册未挂服务）时，已知角色的 getRole/setRole 不是契约化失败而是 **NPE**（myRelServiceMBeanServer 为 null）——测试按 NPE|RSNRE 双兼容断言记录。
2. 管理面 7 属性集合是**完整精确**断言（排序后字符串全等），防内省面漂移。
3. setRole 失败链顺序：null 参数 IAE → checkRoleWriting（类型/度/类匹配 IRVE）→ sendRoleUpdateNotification（RelationNotFoundException when 未挂服务）。

