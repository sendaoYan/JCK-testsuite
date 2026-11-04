# javax.imageio.spi.IIORegistry — DESIGN

## 1. API 功能
`IIORegistry` 是 Image I/O 服务提供者注册表的全局单例实现，`final` 类，继承 `ServiceRegistry`。方法分组：
- 工厂：`static IIORegistry getDefaultInstance()`（按 AppContext/ThreadGroup 返回单例）。
- 类别查询：`Iterator<Class<?>> getCategories()`（继承）。
- 提供者查询：`getServiceProviders(Class,boolean)`、`getServiceProviderByClass(Class)`（继承）。
- 应用类路径扫描：`void registerApplicationClasspathSpis()`。

## 2. Java 用户如何使用
用户通过 `IIORegistry.getDefaultInstance()` 拿唯一实例，然后查询注册表中的标准 SPI。典型组合链：
`IIORegistry.getDefaultInstance()` → `getCategories()` 遍历类别 → `getServiceProviders(ImageReaderSpi.class, true)` 枚举 PNG/JPEG 等 reader 提供者 → `getServiceProviderByClass(spi.getClass())` 按叶类反查同一实例。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/spi/IIORegistry.java`：`getDefaultInstance()` 用 `AppContext.getAppContext()` 缓存单例；构造器私有，注册 18 个标准 SPI（GIF/BMP/WBMP/TIFF/PNG/JPEG 读写器 + File/InputStream/OutputStream/RAF 流 SPI）。
- `src/java.desktop/share/classes/javax/imageio/ImageIO.java`：`ImageIO.getImageReaders(...)` 等大量静态方法内部都先 `IIORegistry.getDefaultInstance()` 再查 `getServiceProviders`。

## 4. 测试用例设计 + 自动化实现（`IIORegistryTest.testBlackBox`）
- `getDefaultInstance()` 两次调用返回同一对象（单例）。
- `getCategories()` 恰好 5 个类别，且包含 5 个标准 SPI 类别。
- `getServiceProviders(ImageReaderSpi.class, true)` 非空、元素均非空、且包含 png reader（格式名大小写不敏感匹配 "png"）。
- `getServiceProviders(ImageInputStreamSpi.class, true)` 非空。
- 取任一 reader SPI，用 `getServiceProviderByClass(spi.getClass())` 反查得到同一实例（`==` 身份相等）。

## 5. 契约深化补充（本轮）
- 注册/注销往返：自写 `ProbeReaderSpi`（`super(注入 vendorName/version)`）注册进全局注册表 → `onRegistration(registry,category)` 收到精确参数（registry==全局实例、category==ImageReaderSpi.class）→ 迭代器可见 → 注销后不可见、`onDeregistration` 一次；`(Object)` 重载与未注册注销均实测；全局改动 finally 恢复。
- 排序往返：set true/重复 false/有序下标/unset 首真再假/未注册对 false/`(a,a)` 自环 `IllegalArgumentException`。
- 过滤器：pngOnly 在 useOrdering 两态下成员一致；rejectAll 空迭代。
- 负例（精确异常探针）：null 注册（两重载）/null 注销/`contains(null)`/未知类别均 `IllegalArgumentException`；null filter `NullPointerException`；`getServiceProviderByClass(null)` IAE、未知类 null；`registerApplicationClasspathSpis()` 幂等。
