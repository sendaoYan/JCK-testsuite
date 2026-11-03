# javax.imageio.spi.ImageTranscoderSpi — DESIGN

## 1. API 功能
`ImageTranscoderSpi` 是 `ImageTranscoder`（格式转换器）的服务提供者抽象基类，继承 `IIOServiceProvider`。方法分组：
- 构造：`ImageTranscoderSpi(String,String)`、`ImageTranscoderSpi()`。
- 配对：`getReaderServiceProviderName()`、`getWriterServiceProviderName()`。
- 实例化：抽象 `createTranscoderInstance()`。

## 2. Java 用户如何使用
从 `IIORegistry.getDefaultInstance().getServiceProviders(ImageTranscoderSpi.class, true)` 枚举转换器 SPI，读 `getReaderServiceProviderName()`/`getWriterServiceProviderName()` 判断支持的读写器对，再 `createTranscoderInstance()` 得到 `ImageTranscoder`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/spi/ImageTranscoderSpi.java`：抽象类，只有 vendor/version 构造与三个抽象方法，无默认实现。
- JDK 内置 `IIORegistry`（`IIORegistry.java` 的 `registerStandardSpis`）不注册任何 `ImageTranscoderSpi`，`ServiceLoader.loadInstalled` 也通常无此提供者，因此默认注册表可能为空。

## 4. 测试用例设计 + 自动化实现（`ImageTranscoderSpiTest.testBlackBox`）
- `getServiceProviders(ImageTranscoderSpi.class, true)` 返回非 null 迭代器。
- 若存在提供者：逐项断言 `getReaderServiceProviderName()`/`getWriterServiceProviderName()` 非 null、`createTranscoderInstance()` 非 null。
- 若为空（JDK 默认情形）：以空迭代器降级通过，不强行假设存在内置转换器。
