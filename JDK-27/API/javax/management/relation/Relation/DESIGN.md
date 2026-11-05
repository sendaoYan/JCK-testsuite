# javax.management.relation.Relation — 设计案

## 1. API 功能
- 接口：关系对象需支持的核心操作。
- 读：`getRole`/`getRoles`/`getAllRoles`/`retrieveAllRoles`/`getRoleCardinality`/`getReferencedMBeans`。
- 写：`setRole`/`setRoles`/`handleMBeanUnregistration`。
- 元数据：`getRelationTypeName`/`getRelationServiceName`/`getRelationId`。

## 2. Java 用户如何使用
由实现类（如 `RelationSupport`）提供方法；用户侧常以该接口引用关系 MBean 并调用 `getRole`/`getAllRoles`。

## 3. 开源代码实际用法调研
- JDK 源码自用：`RelationService` 大量以 `Relation` 接口引用关系对象（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation/RelationService.java`）。
- `RelationSupport`/`RelationSupportMBean` 直接实现/继承该接口。

## 4. 测试用例设计
- 白盒：public 接口、12 个抽象方法签名存在、返回类型正确。
- 轻行为：`RelationSupport` 实现 `Relation`（实现类链接验证）。

## 深化（2026-11 批 L：记录型实现 + 声明异常集）

### 新增覆盖维度
- **接口面精确枚举**：Relation 恰声明 12 个方法（getRole/getRoles/getRoleCardinality/getAllRoles/retrieveAllRoles/setRole/setRoles/handleMBeanUnregistration/getReferencedMBeans/getRelationTypeName/getRelationServiceName/getRelationId）——TreeSet 全等；返回类型（RoleResult/RoleList/Map）。
- **声明异常集**：getRole 恰抛 {IllegalArgumentException, RoleNotFoundException, RelationServiceNotRegisteredException}；setRole 恰抛 6 种（+RelationTypeNotFoundException/InvalidRoleValueException/RelationNotFoundException）——反射锁定。
- **记录型实现（RecRel）**：12 个方法全走 `Relation` 接口引用调用并断言副作用记录（calls 列表/getter 值/setRole 落值/handleMBeanUnregistration 参数捕获与效果/getRoles 混合结果/声明 RoleNotFoundException 透传（消息精确））。
- RelationSupport instanceof Relation 收口。

### 易错规范细节
1. 接口无父接口，getMethods() 即全集；树集全等可防多余默认方法混入。
2. RecRel 演示契约：getRole 未知角色抛 RoleNotFoundException、setRole 无返回值靠副作用、handleMBeanUnregistration 由服务 purge 时回调。

