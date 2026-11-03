# javax.naming.spi.StateFactory — 测试设计

## 1. API 功能
- 服务提供者接口。
- 单一方法：`Object getStateToBind(Object, Name, Context, Hashtable)`（抛 `NamingException`）。

## 2. Java 用户如何使用
- 提供者实现该接口，把对象在绑定前转换为可存储状态；由 `NamingManager.getStateToBind` 调用。
- 返回 null 表示“不做转换”（`NamingManager.getStateToBind` 此时返回原对象）。
- 组合链路：`NamingManager.getStateToBind(obj, name, ctx, env)` → `factory.getStateToBind(...)` → 状态对象或 null。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `javax/naming/spi/NamingManager.getStateToBind`：`answer = factory.getStateToBind(obj, name, nameCtx, environment);`，无工厂时 `return obj;`。
- `com/sun/naming/internal/ResourceManager.getFactories(Context.STATE_FACTORIES, ...)` 枚举状态工厂。

## 4. 测试用例设计
黑盒断言（`StateFactoryTest.testBlackBox`）：
- 匿名实现验证方法收到对象与环境并返回正确转换结果。
- 返回 null 是合法的“不转换”响应。
白盒断言（`testWhiteBox`）：public/interface、无超类、方法签名。
