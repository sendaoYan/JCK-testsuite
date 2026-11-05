# javax.management.relation.RelationSupportMBean — 设计案（JDK-8 侧）

## 1. API 功能
RelationSupport 的管理接口：直接父接口 Relation（12 方法）+ 自有 2 方法。
- **自有**：`Boolean isInRelationService()`（返回包装类型，因此不是 MBean 属性）；`void setRelationServiceManagementFlag(Boolean)` 声明 IllegalArgumentException。
- 加上 MBeanRegistration 后，RelationSupport 的 MBean 属性面为 7 个（AllRoles/ReferencedMBeans/RelationId/RelationServiceManagementFlag/RelationServiceName/RelationTypeName/Role），Role 与 flag 为只写。

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

## 深化（2026-11 批 L：接口面 + flag 契约 + 代理路由）

### 新增覆盖维度
- **接口面精确枚举**：自有方法恰 2 个（isInRelationService/setRelationServiceManagementFlag）；总面 = 12（Relation）+2 = 14（TreeSet 计数）；isInRelationService 返回 **Boolean**（包装类）；setter 恰声明 IllegalArgumentException。
- **flag 契约**：默认 false、TRUE/FALSE 翻转、null → `IllegalArgumentException("Invalid parameter.")`。
- **MBean 注册 + 代理路由**：注册 RelationSupport 为 MBean → `getAttribute(RelationId)` 真值；`setAttribute(RelationServiceManagementFlag, TRUE)` 真生效；`MBeanServerInvocationHandler.newProxyInstance(..., RelationSupportMBean.class, false)` → getRelationId/setRelationServiceManagementFlag 路由生效；**proxy.isInRelationService() → UndeclaredThrowableException(AttributeNotFoundException)**（Boolean 非 is-getter，不构成属性——记录性契约）。
- RelationSupport 接口集（RelationSupportMBean+Relation+MBeanRegistration）断言。

### 易错规范细节（双侧一致）
1. `isInRelationService` 因返回包装 Boolean 不暴露为 MBean 属性，flag 因此是**只写**属性（RelationServiceManagementFlag-W）。
2. 代理读非属性 is-getter 的失败形态是 UndeclaredThrowableException 包装 AttributeNotFoundException，不是直接 AttributeNotFoundException。

