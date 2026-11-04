# javax.imageio.spi.RegisterableService — DESIGN（JDK-8）

## 1. API 功能
`RegisterableService` 是可选注册通知接口（独立接口，无父接口），仅 2 个方法：
- `void onRegistration(ServiceRegistry registry, Class<?> category)`
- `void onDeregistration(ServiceRegistry registry, Class<?> category)`
两者均 public abstract、不声明受检异常。

## 2. Java 用户如何使用
SPI 实现本接口后，`ServiceRegistry` 在注册/注销时自动回调，且把触发回调的 registry 实例与类别 `Class` 原样传入——插件可据此惰性初始化（如读配置）与释放资源。

## 3. 开源代码实际用法调研（来源：OpenJDK 8u 源码）
- `jdk/src/share/classes/javax/imageio/spi/ServiceRegistry.java`：`SubRegistry.register` 内 `if (provider instanceof RegisterableService)` 反射分派 `onRegistration(this, category)`；注销路径对称。**注意：`getCategories()` 迭代器 `remove()` 与 `getServiceProviders` 迭代器 `remove()` 是直接集合操作，绕过该回调（记录的实际行为）。**
- `jdk/src/share/classes/javax/imageio/spi/IIOServiceProvider.java`：基类提供空实现，子类按需覆盖。
- `jdk/test/javax/imageio/spi/DeregisterAllSpiTest.java`：官方回归验证 deregisterAll 驱动 onDeregistration。

## 4. 测试用例设计 + 易错规范细节（`RegisterableServiceTest`）
- 反射精确签名：恰好 2 个声明方法；返回 void；两参数类型 `ServiceRegistry`/`Class`；public abstract；无受检异常。
- 端到端分派：自写 `RecordingSpi extends ImageInputStreamSpi` 在私有注册表注册 → `onRegistration` 一次且 registry/category 与注册调用参数 `==` 相同 → 注销 → `onDeregistration` 一次且参数透传。
- 替换语义：注册同叶类第二实例 → 旧实例收到 onDeregistration（category 透传）、新实例收到自己的 onRegistration。
- `deregisterAll(category)` 同样驱动 onDeregistration。
- 非类别成员：普通 `PlainService implements RegisterableService`（非任何 SPI 类别成员）注册不生效、回调不触发、`contains` false。
- 接口直呼：`svc.onRegistration(reg, cat)`/`onDeregistration` 直接分派计数；实现可接受 null 参数。
- 易错点：回调的 category 是**注册表类别**（如 ImageInputStreamSpi.class），不是提供者叶子类；验证必须用私有注册表避免污染全局单例。
