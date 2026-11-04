# javax.imageio.plugins.jpeg.JPEGImageWriteParam — DESIGN

## 1. API 功能
`JPEGImageWriteParam extends ImageWriteParam`（唯一构造器 `(Locale)`）：JPEG 编码参数。
- 压缩：`unsetCompression()`（重置为 type=JPEG、quality=0.75 的 JPEG 专属默认）、`isCompressionLossless()`（false）、`getCompressionQualityDescriptions()/getCompressionQualityValues()`（Locale.US 下 3 档描述/4 个锚点）。
- 表：`areTablesSet()/setEncodeTables/unsetEncodeTables/getQTables/getDCHuffmanTables/getACHuffmanTables`。
- 其他：`setOptimizeHuffmanTables(boolean)/getOptimizeHuffmanTables()`。
- 能力：`canWriteCompressed()==true`、`canWriteProgressive()==true`；压缩类型全集恰为 `{"JPEG"}`。

## 2. Java 用户如何使用
`new JPEGImageWriteParam(Locale.US)` → `setCompressionMode(MODE_EXPLICIT)`（自动恢复 type=JPEG/quality=0.75）→ `setCompressionQuality(0.9f)` → 可选 `setProgressiveMode(MODE_DEFAULT)`（渐进扫描）与 `setOptimizeHuffmanTables(true)` → `writer.write(null, iioImage, param)`。

## 3. 开源代码实际用法调研（来源：OpenJDK 源码路径）
- `~/git/jdk8u-dev/jdk/src/share/classes/javax/imageio/plugins/jpeg/JPEGImageWriteParam.java`：构造器 `canWriteCompressed=true; compressionTypes={"JPEG"}; progressiveMode=MODE_DISABLED; canWriteProgressive=true`；`unsetCompression` 等方法先 `getCompressionMode()!=MODE_EXPLICIT → ISE`。
- `~/git/jdk8u-dev/jdk/src/share/classes/javax/imageio/ImageWriteParam.java`：`setProgressiveMode` 对 `MODE_EXPLICIT` 抛 **IllegalArgumentException**（不是 UnsupportedOperationException——任务书猜测有误，以实测为准）；`setCompressionMode(EXPLICIT)` 先调 `unsetCompression()`。
- `com/sun/imageio/plugins/jpeg/JPEGImageWriter.java`：`getDefaultWriteParam()` 返回 `JPEGImageWriteParam`。

## 4. 测试用例设计 + 易错规范细节
- 反射断言声明方法集恰 12 个、单 `(Locale)` 构造器、无新公共字段；`new JPEGImageWriteParam(null)` 合法且 locale 保持 null。
- 渐进矩阵（实测）：默认 MODE_DISABLED；DEFAULT/DISABLED/COPY_FROM_METADATA 往返 OK；`MODE_EXPLICIT → IllegalArgumentException`；mode 9/-1 → IAE；压缩模式切换不重置渐进模式。
- 压缩矩阵：非 EXPLICIT 下 getCompressionType/setCompressionQuality/unsetCompression → ISE；进入 EXPLICIT 后 type=JPEG、quality=0.75（JPEG 覆写 unsetCompression 的专属默认）；`setCompressionType("PNG"/"jpeg")` → IAE；null 清除后再 setQuality → ISE；quality -0.1/1.1 → IAE（消息文本 JDK-8/JDK-27 不同，只断言类）、0/1 边界合法；unsetCompression → type=JPEG + quality=0.75（**不是 null**）；退出再进入 EXPLICIT 再度重置。
- 质量刻度（Locale.US，两侧实测一致）：descriptions 长度 3、values 长度 4 且严格递增、端点 0.0/1.0；刻度查询与 setCompressionQuality 互不影响；非 EXPLICIT 下查询 → ISE。
- 编码表矩阵：与 read 侧同构（null/超长/不等长 → IAE；2/2/2 往返；防御性拷贝双向；零长度合法；unset 幂等；失败不污染）。
- optimizeHuffmanTables：默认 false、true/false 往返幂等、与压缩模式互不污染。
- 端到端：progressive(MODE_DEFAULT)+optimize+quality0.9 写出字节非空，读回尺寸往返（有损格式只断言尺寸）。
