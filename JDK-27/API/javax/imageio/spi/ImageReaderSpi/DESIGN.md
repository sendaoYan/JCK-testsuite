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

## 5. 契约深化补充（本轮）
- 真实 PNG SPI 身份：vendor=="Oracle Corporation"/version=="1.0"/`getPluginClassName()` 精确常量；`getInputTypes()` 为 `STANDARD_INPUT_TYPE` 的防御性克隆。
- `canDecodeInput` 契约：魔数 true、文本 false、**null 返回 false（javadoc 写 IAE——记录偏差）**、**4 字节短流返回 false（JDK-8 抛 `EOFException`——版本差异双侧各自断言）**、JPEG 魔数 false。
- `isOwnReader`：用嵌套类二进制名构造匹配/不匹配两种情形（实现按 `getClass().getName()` 与 pluginClassName 比较）。
- 元数据格式访问器：native image 格式名精确 "javax_imageio_png_1.0"；null/未知/标准流格式名 5 组合均 `IllegalArgumentException`。
- 自写 TinyReaderSpi：全部 getter 往返、构造器 null inputTypes IAE。
