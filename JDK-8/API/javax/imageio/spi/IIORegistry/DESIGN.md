# javax.imageio.spi.IIORegistry — DESIGN（JDK-8）

## 1. API 功能
`IIORegistry` 是 Image I/O 服务提供者注册表的全局单例（`final` 类，继承 `ServiceRegistry`，构造器私有）。方法分组：
- 工厂：`static IIORegistry getDefaultInstance()`（JDK-8 按 `ThreadGroup` 级 AppContext 缓存单例）。
- 注册/注销（继承 + 重载）：`registerServiceProvider(Object)`、`deregisterServiceProvider(Object)`、`registerApplicationClasspathSpis()`。
- 查询：`getCategories()`、`getServiceProviders(Class,boolean)`、`getServiceProviders(Class,Filter,boolean)`、`getServiceProviderByClass(Class)`、`contains(Object)`。
- 排序：`setOrdering(Class,T,T)`、`unsetOrdering(Class,T,T)`。

## 2. Java 用户如何使用
用户通过 `getDefaultInstance()` 拿唯一实例，枚举/查询标准 SPI（PNG/JPEG/GIF/BMP/WBMP 读写器、流 SPI），也可注册自写 SPI 临时参与 `ImageIO` 静态工厂的插件选择：注册后 `ImageIO.getImageReaders(...)` 等立刻可见，finally 中注销恢复全局状态。

## 3. 开源代码实际用法调研（来源：OpenJDK 8u 源码）
- `jdk/src/share/classes/javax/imageio/spi/IIORegistry.java`：构造器注册 16 个标准 SPI（GIF/BMP/WBMP/PNG/JPEG 读写器 + File/InputStream/OutputStream/RAF 流 SPI）；`registerApplicationClasspathSpis()` 走 `ServiceRegistry.lookupProviders` 再逐个注册。
- `jdk/src/share/classes/javax/imageio/ImageIO.java`：所有静态工厂先 `IIORegistry.getDefaultInstance()` 再 `getServiceProviders`。
- `jdk/test/javax/imageio/spi/OrderingTest.java`：官方回归演示 setOrdering/unsetOrdering 往返。

## 4. 测试用例设计 + 易错规范细节（`IIORegistryTest`）
- 单例：`getDefaultInstance()` 两次 `==`。
- 注册往返：自写 `ProbeReaderSpi`（`super(注入vendorName/version)`）注册进全局注册表 → `registerServiceProvider` 返回 true → `onRegistration(registry,category)` 收到精确参数（registry==全局实例、category==ImageReaderSpi.class）→ 迭代器可见 → `deregisterServiceProvider` 后不可见、`onDeregistration` 回调一次；`(Object)` 重载可用；注销未注册者无害。全局改动均在 finally 中恢复。
- 排序往返：`setOrdering(png,mine)` true、重复设置 false、有序迭代下标验证、`unsetOrdering` 首真再假、未注册对 false、`(a,a)` 自环抛 `IllegalArgumentException`。
- 过滤器：pngOnly 过滤器在 `useOrdering` 两种状态下成员一致；rejectAll 得空迭代。
- 负例（探针实测精确异常）：`registerServiceProvider(null)`（两个重载）、`deregisterServiceProvider(null)`、`contains(null)`、未知类别 `getServiceProviders`/`setOrdering` 均 `IllegalArgumentException`；`getServiceProviders(class,null,false)` 抛 `NullPointerException`；`getServiceProviderByClass(null)` `IllegalArgumentException`、未知类返回 null；`registerApplicationClasspathSpis()` 可重复调用无副作用。
- 易错点：注册表按"叶子类"存提供者（同叶类重注册会替换旧实例并触发旧实例 onDeregistration）；测试用独立叶子类避免互相替换。
