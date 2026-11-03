# javax.imageio.spi.ImageOutputStreamSpi — DESIGN

## 1. API 功能
`ImageOutputStreamSpi` 是创建 `ImageOutputStream` 的服务提供者抽象基类，继承 `IIOServiceProvider`。方法分组：
- 构造：`ImageOutputStreamSpi(String,String,Class<?>)`、`ImageOutputStreamSpi()`。
- 查询：`getOutputClass()`、`canUseCacheFile()`、`needsCacheFile()`。
- 创建：抽象 `createOutputStreamInstance(Object,boolean,File)`、便捷 `createOutputStreamInstance(Object)`。

## 2. Java 用户如何使用
通过 `IIORegistry.getDefaultInstance().getServiceProviders(ImageOutputStreamSpi.class, false)` 枚举提供者，按 `getOutputClass()` 挑选能处理 `OutputStream` 的 SPI，再调用 `createOutputStreamInstance(byteArrayOutputStream)` 得到 `ImageOutputStream`，`write`/`flush`/`close` 后检查 `ByteArrayOutputStream` 内容。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/com/sun/imageio/spi/OutputStreamImageOutputStreamSpi.java`：`outputClass = OutputStream.class`；`useCache` 为 true 返回 `FileCacheImageOutputStream`，否则返回 `MemoryCacheImageOutputStream`；非 `OutputStream` 输入抛 `IllegalArgumentException`。
- `src/java.desktop/share/classes/javax/imageio/spi/ImageOutputStreamSpi.java`：单参便捷方法等价于 `createOutputStreamInstance(output, true, null)`。

## 4. 测试用例设计 + 自动化实现（`ImageOutputStreamSpiTest.testBlackBox`）
- 枚举全部 `ImageOutputStreamSpi`：每个 `getOutputClass()` 非 null、缓存查询无异常，并找到 `OutputStream` 提供者。
- `getOutputClass()` == `OutputStream.class`，`canUseCacheFile()` 为 true、`needsCacheFile()` 为 false。
- 用 3 参重载（`useCache=false`）创建流，`write`/`flush`/`close` 后断言字节写入 `ByteArrayOutputStream`。
- 用单参便捷重载创建流并 `close`。
- 错误输出类型（String）抛 `IllegalArgumentException`。
