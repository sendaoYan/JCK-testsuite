# javax.management.relation.Relation — 设计案（JDK-8 侧）

## 1. API 功能
外部关系必须实现的接口（无父接口，12 方法）。分组：
- **读**：getRole(String)/getRoles(String[])/getAllRoles()/retrieveAllRoles()/getRoleCardinality(String)。
- **写**：setRole(Role)/setRoles(RoleList)/handleMBeanUnregistration(ObjectName, String)（成员注销回调，由 RelationService purge 时触发）。
- **元数据**：getReferencedMBeans()/getRelationTypeName()/getRelationServiceName()/getRelationId()。
RelationSupport 即其标准实现，供 addRelation 纳管对比。

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

## 深化（2026-11 批 L：记录型实现 + 声明异常集）

### 新增覆盖维度
- **接口面精确枚举**：Relation 恰声明 12 个方法（getRole/getRoles/getRoleCardinality/getAllRoles/retrieveAllRoles/setRole/setRoles/handleMBeanUnregistration/getReferencedMBeans/getRelationTypeName/getRelationServiceName/getRelationId）——TreeSet 全等；返回类型（RoleResult/RoleList/Map）。
- **声明异常集**：getRole 恰抛 {IllegalArgumentException, RoleNotFoundException, RelationServiceNotRegisteredException}；setRole 恰抛 6 种（+RelationTypeNotFoundException/InvalidRoleValueException/RelationNotFoundException）——反射锁定。
- **记录型实现（RecRel）**：12 个方法全走 `Relation` 接口引用调用并断言副作用记录（calls 列表/getter 值/setRole 落值/handleMBeanUnregistration 参数捕获与效果/getRoles 混合结果/声明 RoleNotFoundException 透传（消息精确））。
- RelationSupport instanceof Relation 收口。

### 易错规范细节
1. 接口无父接口，getMethods() 即全集；树集全等可防多余默认方法混入。
2. RecRel 演示契约：getRole 未知角色抛 RoleNotFoundException、setRole 无返回值靠副作用、handleMBeanUnregistration 由服务 purge 时回调。

