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

## 深化（2026-11 批 L：接口面 + flag 契约 + 代理路由）

### 新增覆盖维度
- **接口面精确枚举**：自有方法恰 2 个（isInRelationService/setRelationServiceManagementFlag）；总面 = 12（Relation）+2 = 14（TreeSet 计数）；isInRelationService 返回 **Boolean**（包装类）；setter 恰声明 IllegalArgumentException。
- **flag 契约**：默认 false、TRUE/FALSE 翻转、null → `IllegalArgumentException("Invalid parameter.")`。
- **MBean 注册 + 代理路由**：注册 RelationSupport 为 MBean → `getAttribute(RelationId)` 真值；`setAttribute(RelationServiceManagementFlag, TRUE)` 真生效；`MBeanServerInvocationHandler.newProxyInstance(..., RelationSupportMBean.class, false)` → getRelationId/setRelationServiceManagementFlag 路由生效；**proxy.isInRelationService() → UndeclaredThrowableException(AttributeNotFoundException)**（Boolean 非 is-getter，不构成属性——记录性契约）。
- RelationSupport 接口集（RelationSupportMBean+Relation+MBeanRegistration）断言。

### 易错规范细节（双侧一致）
1. `isInRelationService` 因返回包装 Boolean 不暴露为 MBean 属性，flag 因此是**只写**属性（RelationServiceManagementFlag-W）。
2. 代理读非属性 is-getter 的失败形态是 UndeclaredThrowableException 包装 AttributeNotFoundException，不是直接 AttributeNotFoundException。

