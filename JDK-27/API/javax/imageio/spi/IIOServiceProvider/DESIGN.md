# javax.imageio.spi.IIOServiceProvider — DESIGN

## 1. API 功能
`IIOServiceProvider` 是所有 Image I/O 服务提供者接口（SPI）的抽象基类，实现 `RegisterableService`。方法分组：
- 构造：`IIOServiceProvider(String vendorName, String version)`、`IIOServiceProvider()`。
- 注册回调：`onRegistration(ServiceRegistry, Class<?>)`、`onDeregistration(ServiceRegistry, Class<?>)`（默认空实现）。
- 查询：`getVendorName()`、`getVersion()`、抽象 `getDescription(Locale)`。

## 2. Java 用户如何使用
用户不直接实例化抽象类，而是通过 `ImageIO.getImageWritersByFormatName("png").next().getOriginatingProvider()` 拿到具体 SPI（如 PNG writer SPI），再调用 `getVendorName()`/`getVersion()`/`getDescription(Locale.US)`。注册回调由 `ServiceRegistry` 在注册/注销时自动调用，用户通常覆盖它们做环境校验。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/spi/IIOServiceProvider.java`：构造器校验 `vendorName`/`version` 非 null，否则抛 `IllegalArgumentException`；`onRegistration`/`onDeregistration` 默认空实现。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/png/PNGImageWriterSpi.java`：通过 `super(vendorName, version, ...)` 传入 "Oracle Corporation" / "1.0"，覆盖 `getDescription` 返回 "Standard PNG image writer"。

## 4. 测试用例设计 + 自动化实现（`IIOServiceProviderTest.testBlackBox`）
- 自定义 `SampleProvider` 子类走构造 + `getVendorName`/`getVersion`/`getDescription` 往返。
- 用 ImageIO 的真实 PNG writer SPI 断言三个查询方法非 null。
- 在默认 `IIORegistry` 上调用 `onRegistration`/`onDeregistration`，断言无异常返回。
- 通过 `super(null, "1.0")` 与 `super("Vendor", null)` 子类验证构造器 null 校验抛 `IllegalArgumentException`。
- 断言 `IIOServiceProvider` 实例是 `RegisterableService`。

## 5. 契约深化补充（本轮）
- 无参构造实测：`getVendorName()`/`getVersion()` 均为 null（javadoc 未声明的记录行为）；空串 `""` 厂商/版本被接受。
- 回调透传：自写 `RecordingProvider extends ImageInputStreamSpi` 在私有注册表验证 onRegistration/onDeregistration 收到的 registry/category 与注册参数 `==` 相同；同叶类替换触发旧实例 onDeregistration。
- 真实提供者：png writer SPI vendor=="Oracle Corporation"、version=="1.0"、`getDescription(Locale.US)`==`getDescription(null)`（未本地化）。
