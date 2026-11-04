# javax.imageio.plugins.bmp.BMPImageWriteParam — DESIGN

## 1. API 功能
`BMPImageWriteParam extends ImageWriteParam`：BMP 编码参数。自有 API 仅 `BMPImageWriteParam()`/`BMPImageWriteParam(Locale)`、`setTopDown(boolean)`、`isTopDown()`；压缩配置全部继承 `ImageWriteParam`。
压缩类型全集（实测两侧 JDK 一致）：`{BI_RGB, BI_RLE8, BI_RLE4, BI_BITFIELDS, BI_JPEG, BI_PNG}`（6 个，含顺序）。能力：`canWriteCompressed()==true`、`canWriteProgressive()==false`、`canWriteTiles()==false`、`canOffsetTiles()==false`。

## 2. Java 用户如何使用
`new BMPImageWriteParam()` 或 `writer.getDefaultWriteParam()` 强转；`setTopDown(true)` 写"自顶向下"行序 BMP；压缩：先 `setCompressionMode(MODE_EXPLICIT)` 再 `setCompressionType("BI_RLE8")`，最后 `setCompressionQuality`。

## 3. 开源代码实际用法调研（来源：OpenJDK 源码路径）
- `~/git/jdk8u-dev/jdk/src/share/classes/javax/imageio/plugins/bmp/BMPImageWriteParam.java`：构造器设 `compressionTypes = BMPCompressionTypes.getCompressionTypes()`、`canWriteCompressed=true`、`compressionType=BI_RGB`（字段级默认，进入 EXPLICIT 模式会被 unset）。
- `~/git/jdk8u-dev/jdk/src/share/classes/com/sun/imageio/plugins/bmp/BMPCompressionTypes.java`：`compressionTypeNames = {BI_RGB, BI_RLE8, BI_RLE4, BI_BITFIELDS, BI_JPEG, BI_PNG}`，`getCompressionTypes()` 返回 clone。
- `~/git/jdk8u-dev/jdk/src/share/classes/javax/imageio/ImageWriteParam.java`：`setCompressionMode(EXPLICIT)` 会先 `unsetCompression()`（故类型清空、quality 回 1.0）；`setCompressionType` 在非 EXPLICIT 抛 ISE、未知串抛 IAE、null 清除；`setCompressionQuality` 顺序检查 mode→type→range。
- `com/sun/imageio/plugins/bmp/BMPImageWriter.java`：RLE8 与 24bpp 不兼容抛 `IOException`（JDK-8 与 JDK-27 消息文本不同，只断言异常类）。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵：no-arg / Locale.US / null；`getLocale` 往返（no-arg 与 null 均为 null）；反射断言恰好两个公共构造器。
- topDown：默认 false、true/false 往返幂等、与压缩状态机互不污染。
- 压缩类型：精确全集（6 个，含顺序）；`getCompressionTypes()` 每次返回新数组、改返回数组不影响后续调用。
- MODE 状态机（继承逻辑在 BMP 上端到端）：非 EXPLICIT 下 set/getCompressionType、get/setCompressionQuality、unsetCompression 全部 ISE；EXPLICIT 进入后 type 为 null；未知/大小写错 → IAE；null 清除后再 setCompressionQuality → ISE；quality 越界 (-0.1/1.5) → IAE，0/1 边界合法；`setCompressionMode(9/-1)` → IAE；unsetCompression 清 type、quality 回 1.0；失败调用不污染既有状态。
- 渐进不支持：`setProgressiveMode(任意)`/`getProgressiveMode` → UnsupportedOperationException。
- NaN quality：**通过** `<0||>1` 检查被原样接受（实测，规范未写）。
- 端到端：topDown 写 8x6 往返像素级相等；BI_RLE8×24bpp → IOException（仅断言类）；BI_RLE8×TYPE_BYTE_INDEXED（5x4 奇数宽）→ 尺寸+像素往返。
- `getCompressionQualityDescriptions/Values` 在 BMP 上返回 null（无本地化刻度）；`getBitRate== -1.0`。
