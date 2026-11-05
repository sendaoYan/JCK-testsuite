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

