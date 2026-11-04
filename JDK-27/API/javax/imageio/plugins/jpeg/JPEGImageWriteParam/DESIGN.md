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

## 5. 契约深化补充（evtplug，2026-11）
- 反射断言声明方法集恰 12 个、单 `(Locale)` 构造器。
- 渐进矩阵：默认 MODE_DISABLED；DEFAULT/DISABLED/COPY_FROM_METADATA 往返；**MODE_EXPLICIT → IllegalArgumentException（不是 UOE，实测）**；mode 9/-1 → IAE。
- 压缩矩阵：进入 EXPLICIT 自动恢复 type=JPEG/quality=0.75（JPEG 覆写 unsetCompression 的专属默认，非 null）；`setCompressionType("PNG"/"jpeg")` IAE；unset 后 type=JPEG。
- 质量刻度（Locale.US）：descriptions 长 3、values 长 4 严格递增端点 0/1；非 EXPLICIT 下查询 ISE。
- encodeTables 矩阵（null/超长/不等长 IAE；防御拷贝双向；零长度合法；unset 幂等）；optimizeHuffmanTables 默认 false 幂等往返。
- 端到端：progressive+optimize+quality0.9 写出非空、读回尺寸往返。
