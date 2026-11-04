# javax.imageio.ImageWriter — DESIGN

## 1. API 功能
`ImageWriter` 是所有编码器的抽象基类，与 `ImageReader` 镜像但不对称：
- 输出：`setOutput(Object)`（有 provider 时按 `getOutputTypes()` 校验 IAE；无 provider 时接受任意对象 —— 与 reader 必须为 ImageInputStream 相反）；`getOutput()`；`reset()`。
- 写入：`write(RenderedImage)` 委托 `write(null, new IIOImage(im,null,null), null)`；`write(IIOImage)` 委托 3 参形式；`write(IIOMetadata,IIOImage,ImageWriteParam)` 由子类实现（基类不做 null 防御，null 直达子类）。
- 能力矩阵：`canInsertImage/canRemoveImage/canReplacePixels/canReplaceStreamMetadata/canReplaceImageMetadata/canWriteEmpty/canInsertEmpty` 无输出时 ISE、有输出时返回 false；对应变更方法（writeInsert/removeImage/replacePixels/prepareXxx/endXxx）无输出 ISE、有输出 UOE。特例：`canWriteSequence` 完全不检查输出（无输出时直接返回 false）；`endWriteEmpty/endInsertEmpty` 无论有无输出都是 ISE（无 prepare）。
- 元数据：`getDefaultStreamMetadata/getDefaultImageMetadata/convertStreamMetadata/convertImageMetadata`。
- 监听器/abort/locale：与 reader 对应的 progress/warning 监听器、`abort()`、`getAvailableLocales/setLocale`（非列表 locale IAE）、`getDefaultWriteParam()` 继承 locale。

## 2. Java 用户如何使用
`ImageWriter w = ImageIO.getImageWritersByFormatName("png").next(); w.setOutput(new MemoryCacheImageOutputStream(bos)); w.write(bi); w.dispose();`

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/ImageWriter.java`：`write(RenderedImage)` 实现 `write(null, new IIOImage(im, null, null), null)`；基类 `write(...)` 对 image 为 null 不设防（IAE 由具体子类抛）；`processWarningOccurred(int,String)` 在有监听器且 warning==null 时抛 IAE，无监听器时静默。
- `com/sun/imageio/plugins/png/PNGImageWriter.java`：`write()` 中 `abortRequested()` 检查 → `processWriteAborted`，否则 `processImageComplete`。
- JDK-8 `PNGImageWriteParam.canWriteCompressed()==false`（`setCompressionMode(EXPLICIT)` UOE）；JDK-9+ 变为 true 且可设质量 —— 跨版本行为差异，测试两侧各自断言。
- `test/jdk/javax/imageio/plugins/png/WriteProgressive.java`、`RoundTripTest.java` 覆盖写入与读回。

## 4. 测试用例设计 + 自动化实现（`ImageWriterTest`，双轨：真实 PNG + 自写 StubWriter）
- 白盒：全部方法/字段清单（保留原有）。
- 真实 PNG 轨：无输出 write ISE；`setOutput("banana")` IAE；null IIOImage/RenderedImage IAE；locale 非 IAE/合法往返；null 监听器 no-op；convertStreamMetadata(null)→null、convertImageMetadata(null,its,null) 非空；canWriteRasters false、缩略图 0/无首选尺寸；写读往返（尺寸+两角像素）；reset；JDK-8 特有：PNG 默认参数 canWriteCompressed false、canWriteProgressive true、setCompressionMode(EXPLICIT) UOE。
- 能力矩阵：约 20 个「无输出 ISE」探针 + 「有输出 false/UOE」矩阵（含 replacePixels(Raster)、prepareWriteEmpty/prepareInsertEmpty、endInsertEmpty），endWriteEmpty 两侧均 ISE；canWriteSequence 无输出不抛。
- StubWriter 轨：provider 决定 output 类型校验；无 provider 接受任意输出（不对称）；`write(RenderedImage)` 包装为 `IIOImage(im,null,null)` 且 streamMeta/param 为 null；`write(null IIOImage)` 穿透基类直达子类；3 参 write 全参透传；convert* 透传。
- Stub 回调轨：双监听器计数、移除、removeAll；abort-from-listener → writeAborted 且无 imageComplete；陈旧 abort 被下一次 write 清零；`processWarningOccurred(0,null)` 仅在有警告监听器时 IAE；locale 契约与 `getDefaultWriteParam().getLocale()` 继承；endWriteEmpty ISE。
