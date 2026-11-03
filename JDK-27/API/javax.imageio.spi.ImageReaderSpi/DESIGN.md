# javax.imageio.spi.ImageReaderSpi — DESIGN

## 1. API 功能
`ImageReaderSpi` 是 `ImageReader` 的服务提供者抽象基类，继承 `ImageReaderWriterSpi`。方法分组：
- 输入类型：`getInputTypes()`、静态常量 `STANDARD_INPUT_TYPE`。
- 能力判断：抽象 `canDecodeInput(Object)`。
- 实例化：`createReaderInstance()`、抽象 `createReaderInstance(Object)`。
- 归属/配对：`isOwnReader(ImageReader)`、`getImageWriterSpiNames()`。

## 2. Java 用户如何使用
从 `IIORegistry` 找 png reader SPI 后，`getInputTypes()` 查看可接受输入，`canDecodeInput(imageInputStream)` 探测 PNG 魔数，`createReaderInstance()` 创建 reader，`isOwnReader(reader)` 校验归属，最后 `reader.dispose()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/com/sun/imageio/plugins/png/PNGImageReaderSpi.java`：`canDecodeInput` 用 `mark`/`reset` 读取前 8 字节核对 PNG 签名 `0x89 P N G 0x0D 0x0A 0x1A 0x0A`；`createReaderInstance(Object)` 返回 `new PNGImageReader(this)`；`writerSpiNames = ["com.sun.imageio.plugins.png.PNGImageWriterSpi"]`。
- `src/java.desktop/share/classes/javax/imageio/spi/ImageReaderSpi.java`：`isOwnReader` 默认比较 `reader.getClass().getName()` 与 `pluginClassName`；`createReaderInstance()` 默认 `createReaderInstance(null)`。

## 4. 测试用例设计 + 自动化实现（`ImageReaderSpiTest.testBlackBox`）
- 定位 png reader SPI，断言格式名包含 "png"。
- `getInputTypes()` 非空且包含 `ImageInputStream.class`。
- 用真实 PNG 签名字节流经 `ImageIO.createImageInputStream` 后 `canDecodeInput` 返回 true；传入 String 返回 false。
- `getImageWriterSpiNames()` 非空且首项包含 "PNGImageWriterSpi"。
- `createReaderInstance()` 与 `createReaderInstance(null)` 均返回 reader，`isOwnReader` 返回 true；`isOwnReader(null)` 抛 `IllegalArgumentException`；最后 `dispose`。
