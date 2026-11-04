# javax.naming.spi.DirObjectFactory — 测试设计

## 1. API 功能
- 继承 `ObjectFactory`。
- 目录对象工厂方法：`getObjectInstance(Object, Name, Context, Hashtable, Attributes)`。
- 从 `ObjectFactory` 继承 4 参 `getObjectInstance(Object, Name, Context, Hashtable)`。

## 2. Java 用户如何使用
- 服务提供者实现 `DirObjectFactory`，把 `Reference`/`Referenceable` 及目录属性还原为对象。
- `DirectoryManager.getObjectInstance` 优先调用 5 参方法（若工厂是 `DirObjectFactory`），否则回退到 4 参 `ObjectFactory` 方法。
- 组合链路：`getObjectInstance(ref, name, ctx, env, attrs)` → 工厂创建并返回对象。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `com/sun/naming/internal/NamingManagerHelper.getDirObjectInstance`：`if (factory instanceof DirObjectFactory) return ((DirObjectFactory)factory).getObjectInstance(refInfo, name, nameCtx, environment, attrs);` 否则调用 4 参方法。
- LDAP 目录 provider（`com/sun/jndi/ldap/Obj` 等）通过 `DirectoryManager.getObjectInstance` 还原对象。

## 4. 测试用例设计
黑盒断言（`DirObjectFactoryTest.testBlackBox`）：
- 匿名实现验证 5 参与继承的 4 参方法各自独立可调用、返回值正确。
白盒断言（`testWhiteBox`）：public/interface、继承 `ObjectFactory`、两个重载精确签名。
