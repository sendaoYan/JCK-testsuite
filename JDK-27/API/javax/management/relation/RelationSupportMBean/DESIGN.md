# javax.management.relation.RelationSupportMBean — 设计案

## 1. API 功能
- 接口，继承 `Relation`。
- 新增两个方法：`isInRelationService()`（是否仍被关系服务管理）、`setRelationServiceManagementFlag(Boolean)`（更新该内部标志，null 抛 IAE）。

## 2. Java 用户如何使用
由 `RelationSupport` 实现；用户以该接口引用标准关系 MBean 时调用 `isInRelationService()` 判断状态。

## 3. 开源代码实际用法调研
- JDK 源码自用：`RelationSupport implements RelationSupportMBean`（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation/RelationSupport.java`）。
- 说明：纯 MBean 管理接口，无独立开源生态用法。

## 4. 测试用例设计
- 白盒：public 接口、直接父接口为 Relation、两个新增方法存在。
- 轻行为：`RelationSupport` 实现该接口（实现类链接验证）。
