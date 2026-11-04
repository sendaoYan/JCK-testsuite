# javax.imageio.plugins.jpeg.JPEGImageWriteParam — DESIGN

## 1. API 功能
`JPEGImageWriteParam` 是 `ImageWriteParam` 的子类，为内置 JPEG 编码器提供参数。方法分组：
- 构造器：`JPEGImageWriteParam(Locale)`。
- 压缩：`isCompressionLossless()`（恒 false）、`unsetCompression()`、`getCompressionQualityDescriptions()`、`getCompressionQualityValues()`，以及继承的 `setCompressionQuality/getCompressionQuality`。
- 表设置：`setEncodeTables(...)`、`unsetEncodeTables()`、`areTablesSet()`、`getQTables()`、`getDCHuffmanTables()`、`getACHuffmanTables()`。
- Huffman 优化：`setOptimizeHuffmanTables(boolean)`、`getOptimizeHuffmanTables()`。

## 2. Java 用户如何使用
`new JPEGImageWriteParam(Locale.US)` 或经 `ImageWriter.getDefaultWriteParam()` 得到实例，`setCompressionMode(MODE_EXPLICIT)` + `setCompressionQuality(0.9f)` 控制质量；必要时 `setEncodeTables(...)` 指定量化/Huffman 表、`setOptimizeHuffmanTables(true)` 让编码器优化 Huffman 表；最后传给 `ImageWriter.write(...)`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/jpeg/JPEGImageWriteParam.java`：默认 `compressionQuality = JPEG.DEFAULT_QUALITY`（0.75）、`compressionType="JPEG"`、`MODE_EXPLICIT`；`isCompressionLossless()` 恒返回 false。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/jpeg/JPEGImageWriter.java`：`getDefaultWriteParam()` 返回 `JPEGImageWriteParam`，写图时读取质量与表。
- JDK 自带测试 `test/jdk/javax/imageio/plugins/jpeg/` 有压缩质量与编码表往返测试。

## 4. 测试用例设计 + 自动化实现（`JPEGImageWriteParamTest.testBlackBox`）
- 默认压缩状态：`MODE_EXPLICIT`、`"JPEG"`、`isCompressionLossless()==false`、质量 0.75。
- 质量往返（0.5/1.0/0.25→unset 恢复 0.75）、质量描述/值数组（长度 3、递增）。
- `setOptimizeHuffmanTables` 往返（默认 false）。
- `setEncodeTables/unsetEncodeTables/areTablesSet` 与三类表往返；null 与 DC/AC 不等长抛异常。
- 真实 JPEG 写往返：`getDefaultWriteParam()` 是 `JPEGImageWriteParam`，`setCompressionQuality(0.9f)` 后写出字节非空。
