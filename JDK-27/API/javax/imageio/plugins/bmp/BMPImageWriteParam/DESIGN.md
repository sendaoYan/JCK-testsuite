# javax.imageio.plugins.bmp.BMPImageWriteParam — DESIGN

## 1. API 功能
`BMPImageWriteParam` 是 `ImageWriteParam` 的子类，为内置 BMP 编码器提供写出参数。方法分组：
- 构造器：`BMPImageWriteParam()`、`BMPImageWriteParam(Locale)`。
- 布局：`setTopDown(boolean)`、`isTopDown()`（默认 bottom-up，即 `false`）。
- 继承的压缩参数：`canWriteCompressed()`、`getCompressionMode()/setCompressionMode(int)`、`getCompressionType()/setCompressionType(String)`、`getCompressionTypes()`、`getCompressionQuality()`。

## 2. Java 用户如何使用
用户直接 `new BMPImageWriteParam()`（或经 `ImageWriter.getDefaultWriteParam()` 得到实例），然后 `setTopDown(true)` 选择 top-down 行序，`setCompressionMode(MODE_EXPLICIT)` + `setCompressionType("BI_RGB")` 指定压缩，最后把参数传给 `ImageWriter.write(null, new IIOImage(img, null, null), param)`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/bmp/BMPImageWriteParam.java`：构造器里把 `compressionTypes` 设为 `BMPCompressionTypes.getCompressionTypes()`，默认 `compressionMode = MODE_COPY_FROM_METADATA`、`compressionType = "BI_RGB"`。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/bmp/BMPImageWriter.java`：`getDefaultWriteParam()` 返回 `BMPImageWriteParam`，`write()` 时读取 `topDown`/压缩类型决定写出方式。
- `test/jdk/javax/imageio/plugins/bmp/` 下若干 BMP 读写测试使用 `ImageIO.getImageWritersByFormatName("bmp")` 与内存流往返。

## 4. 测试用例设计 + 自动化实现（`BMPImageWriteParamTest.testBlackBox`）
- `topDown` 默认 `false`，`setTopDown(true/false)` 往返。
- `Locale` 构造器默认值一致。
- 继承的压缩状态：默认 `MODE_COPY_FROM_METADATA`、`"BI_RGB"`、`getCompressionTypes()` 含 `BI_RGB`/`BI_RLE8`、显式模式设置 `BI_RGB`。
- 用真实 BMP `ImageWriter` 做内存写+读往返：验证 `getDefaultWriteParam()` 是 `BMPImageWriteParam`、`setTopDown(true)` 后写出字节可读回、尺寸与像素一致。
