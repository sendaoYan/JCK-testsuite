# javax.naming.spi.InitialContextFactoryBuilder — 测试设计

## 1. API 功能
- 服务提供者接口（工厂的工厂）。
- 单一方法：`InitialContextFactory createInitialContextFactory(Hashtable<?, ?>)`（抛 `NamingException`）。

## 2. Java 用户如何使用
- 通过 `NamingManager.setInitialContextFactoryBuilder(builder)` 安装；之后 `getInitialContext` 用 builder 创建工厂而非读系统属性。
- 组合链路：`builder.createInitialContextFactory(env)` → `InitialContextFactory` → `factory.getInitialContext(env)`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `javax/naming/spi/NamingManager.getInitialContext`：`if (builder != null) factory = builder.createInitialContextFactory(env);`。
- `NamingManager.setInitialContextFactoryBuilder` 安装 builder 以覆盖默认的类名加载策略。

## 4. 测试用例设计
黑盒断言（`InitialContextFactoryBuilderTest.testBlackBox`）：
- 匿名实现验证 `createInitialContextFactory` 收到同一环境、返回产出的工厂，且该工厂可继续调用。
白盒断言（`testWhiteBox`）：public/interface、无超类、方法签名。
