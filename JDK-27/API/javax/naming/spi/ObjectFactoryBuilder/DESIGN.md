# javax.naming.spi.ObjectFactoryBuilder — 测试设计

## 1. API 功能
- 服务提供者接口（对象工厂的工厂）。
- 单一方法：`ObjectFactory createObjectFactory(Object, Hashtable<?, ?>)`（抛 `NamingException`）。

## 2. Java 用户如何使用
- 通过 `NamingManager.setObjectFactoryBuilder(builder)` 安装，覆盖默认的对象工厂加载策略。
- 组合链路：`builder.createObjectFactory(obj, env)` → `ObjectFactory` → `factory.getObjectInstance(...)`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `com/sun/naming/internal/NamingManagerHelper.getObjectInstance`：`if (builder != null) { factory = builder.createObjectFactory(refInfo, environment); return factory.getObjectInstance(...); }`。
- `javax/naming/spi/NamingManager.setObjectFactoryBuilder` 安装 builder（同一 JVM 只允许安装一次，否则 `IllegalStateException`）。

## 4. 测试用例设计
黑盒断言（`ObjectFactoryBuilderTest.testBlackBox`）：
- 匿名实现验证 `createObjectFactory` 收到同一对象与环境、返回非空工厂，且产出的工厂可继续调用并返回正确结果。
白盒断言（`testWhiteBox`）：public/interface、无超类、方法签名。
