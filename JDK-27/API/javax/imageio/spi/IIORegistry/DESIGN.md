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
