# javax.naming.spi.ObjectFactory — 测试设计

## 1. API 功能
- 服务提供者接口。
- 单一方法：`Object getObjectInstance(Object, Name, Context, Hashtable)`（抛 `Exception`）。

## 2. Java 用户如何使用
- 提供者实现该接口，把 `Reference`/`Referenceable`/URL 还原为对象；由 `NamingManager.getObjectInstance` 调用。
- 组合链路：`NamingManager.getObjectInstance(ref, name, ctx, env)` → `factory.getObjectInstance(...)` → 对象。
- 无匹配工厂时 `NamingManager.getObjectInstance` 返回原 `refInfo`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `com/sun/naming/internal/NamingManagerHelper.getObjectInstance` 调用 `factory.getObjectInstance(refInfo, name, nameCtx, environment)`。
- `com/sun/jndi/ldap/Obj.java` 使用 `NamingManager.getObjectInstance` 还原 LDAP 对象。

## 4. 测试用例设计
黑盒断言（`ObjectFactoryTest.testBlackBox`）：
- 匿名实现验证方法收到全部四个参数（对象、name、context、environment）并返回正确结果。
白盒断言（`testWhiteBox`）：public/interface、无超类、方法签名。
