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
