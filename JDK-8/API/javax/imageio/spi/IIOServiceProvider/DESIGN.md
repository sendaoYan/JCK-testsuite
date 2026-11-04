# javax.imageio.spi.IIOServiceProvider — DESIGN（JDK-8）

## 1. API 功能
`IIOServiceProvider` 是所有 Image I/O SPI 的公共抽象基类（实现 `RegisterableService`）。方法分组：
- 构造：`IIOServiceProvider(String vendorName, String version)`（null 参数抛 `IllegalArgumentException`）、无参构造（**实测 vendor/version 留 null**，javadoc 未声明）。
- 标识：`getVendorName()`、`getVersion()`、`getDescription(Locale)`（抽象）。
- 回调：`onRegistration(ServiceRegistry,Class)` / `onDeregistration(ServiceRegistry,Class)`（默认空实现）。

## 2. Java 用户如何使用
用户写插件 SPI 时继承本类，在构造器里 `super("myVendor","1.0")` 注入标识，按需覆盖两个回调在注册/注销时感知注册表与类别；`getDescription` 提供本地化描述。

## 3. 开源代码实际用法调研（来源：OpenJDK 8u 源码）
- `jdk/src/share/classes/javax/imageio/spi/ImageReaderSpi.java` 等：所有标准插件 SPI 构造器 `super("Oracle Corporation", "1.0")` 硬编码厂商与版本。
- `jdk/src/share/classes/javax/imageio/spi/ServiceRegistry.java`：`SubRegistry.register/deregister` 通过 `RegisterableService` 接口反射分派回调，把 registry 与 category 原样传入。
- `jdk/src/share/classes/javax/imageio/spi/IIORegistry.java`：18 个标准 SPI 依赖基类构造器注入 "Oracle Corporation"/"1.0"。

## 4. 测试用例设计 + 易错规范细节（`IIOServiceProviderTest`）
- 构造器实测：无参构造 `getVendorName()`/`getVersion()` 均为 null（记录的实际行为）；空串 `""` 厂商/版本被接受；null 厂商或 null 版本抛 `IllegalArgumentException`（精确类）。
- 回调透传：自写 `RecordingProvider extends ImageInputStreamSpi` 在私有注册表（类别仅 ImageInputStreamSpi）注册/注销，断言回调收到的 registry/category 与注册调用参数逐字相同（`==`）。
- 同叶类替换：注册第二个同叶类实例时旧实例收到 onDeregistration。
- 真实提供者：全局注册表中的 PNG writer SPI `getVendorName()=="Oracle Corporation"`、`getVersion()=="1.0"`、`getDescription(Locale.US)` 与 `getDescription(null)` 一致（未本地化）。
- 易错点：厂商/版本断言依赖 OpenJDK 硬编码常量（跨发行版稳定）；回调验证必须用私有 `ServiceRegistry`，避免污染全局 `IIORegistry`。
