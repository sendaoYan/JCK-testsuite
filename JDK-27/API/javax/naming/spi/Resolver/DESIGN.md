# javax.naming.spi.Resolver — 测试设计

## 1. API 功能
- 服务提供者接口，支持将名称部分解析到特定上下文类型。
- 方法（均抛 `NamingException`）：
  - `ResolveResult resolveToClass(Name, Class<? extends Context>)`
  - `ResolveResult resolveToClass(String, Class<? extends Context>)`

## 2. Java 用户如何使用
- 实现者（如 `ContinuationContext`）把名称解析到指定上下文类型，返回 `ResolveResult`（已解析对象 + 剩余名）。
- 组合链路：`resolver.resolveToClass(name, Context.class)` → `ResolveResult` → `getResolvedObj()/getRemainingName()`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `javax/naming/spi/ContinuationContext` 实现 `Resolver`，`resolveToClass` 返回 `ResolveResult`。
- `com/sun/jndi/toolkit/ctx/ComponentContext` 等命名工具依赖 `Resolver` 完成逐步解析。

## 4. 测试用例设计
黑盒断言（`ResolverTest.testBlackBox`）：
- 匿名实现验证两个重载各自独立调用，`Name` 与 `String` 版本分别触发对应方法并返回预期 `ResolveResult`。
白盒断言（`testWhiteBox`）：public/interface、无超类、两个重载精确签名。
