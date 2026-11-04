# javax.imageio.spi.ImageInputStreamSpi — DESIGN

## 1. API 功能
`ImageInputStreamSpi` 是创建 `ImageInputStream` 的服务提供者抽象基类，继承 `IIOServiceProvider`。方法分组：
- 构造：`ImageInputStreamSpi(String,String,Class<?>)`、`ImageInputStreamSpi()`。
- 查询：`getInputClass()`、`canUseCacheFile()`、`needsCacheFile()`。
- 创建：抽象 `createInputStreamInstance(Object,boolean,File)`、便捷 `createInputStreamInstance(Object)`。

## 2. Java 用户如何使用
通过 `IIORegistry.getDefaultInstance().getServiceProviders(ImageInputStreamSpi.class, false)` 枚举提供者，按 `getInputClass()` 挑选能处理 `InputStream`/`File` 的 SPI，再调用 `createInputStreamInstance(byteArrayInputStream)` 得到 `ImageInputStream` 后 `readFully`/`read` 读取并 `close`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/com/sun/imageio/spi/InputStreamImageInputStreamSpi.java`：`inputClass = InputStream.class`，`canUseCacheFile()` 返回 true、`needsCacheFile()` 返回 false；`useCache` 为 true 时返回 `FileCacheImageInputStream`，否则返回 `MemoryCacheImageInputStream`；非 `InputStream` 输入抛 `IllegalArgumentException`。
- `src/java.desktop/share/classes/javax/imageio/spi/ImageInputStreamSpi.java`：单参便捷方法等价于 `createInputStreamInstance(input, true, null)`。

## 4. 测试用例设计 + 自动化实现（`ImageInputStreamSpiTest.testBlackBox`）
- 枚举全部 `ImageInputStreamSpi`：每个 `getInputClass()` 非 null、缓存查询无异常，并找到 `InputStream` 提供者。
- `getInputClass()` == `InputStream.class`，`canUseCacheFile()` 为 true、`needsCacheFile()` 为 false。
- 用 3 参重载（`useCache=false`）创建流，`readFully` 断言字节往返、再 `read()==-1` 断言耗尽，然后 `close`。
- 用单参便捷重载创建流并 `close`。
- 错误输入类型（String）抛 `IllegalArgumentException`。

## 5. 契约深化补充（本轮）
- 注册表实测：该类别 ≥3 个提供者；inputClass 集合含 InputStream/File/RandomAccessFile；vendor=="Oracle Corporation"。
- 负例逐 SPI：`createInputStreamInstance(null)` 与错误类型在**每个**注册 SPI 上均抛 `IllegalArgumentException`（两重载一致）。
- 自写 RecordingInputSpi：默认 `canUseCacheFile()`/`needsCacheFile()` 均 false；三参工厂 `(input,useCache,cacheDir)` 透传记录；一参工厂实测以 `useCache==true, cacheDir==null` 硬编码转发（记录行为）。
- File SPI 往返：临时文件写入 → `createInputStreamInstance(file)` 读回一致 → finally 删除。
