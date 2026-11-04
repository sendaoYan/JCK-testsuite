# javax.imageio.ImageReader — DESIGN

## 1. API 功能
`ImageReader` 是所有解码器的抽象基类，一半方法为「子类必须实现」的抽象/可选方法，另一半为基于 `input`/provider 的守卫与公共流程（监听器分派、abort 协议、参数校验、区域计算）。
- 输入：`setInput(Object,boolean,boolean)`（无 provider 时仅接受 `ImageInputStream`，否则 IAE；有 provider 时按 `getInputTypes()` 校验）；`getInput()`；`reset()`。
- 查询：`getFormatName`（无 provider 抛 NPE）、`getWidth/getHeight/getNumImages`（无输入 ISE；seekForwardOnly 且 allowSearch 时 ISE）、`getNumThumbnails` 等。
- 读取：`read(int,ImageReadParam)` 及 readAll 双形态（int 形式与 `Iterator<ImageReadParam>` 形式）；`readAsRenderedImage`、`readTile`、`readTileRaster`、`readRaster`。
- 元数据：`getStreamMetadata/ImageMetadata`（format 名分派：standard/native/extra，未知 format 返回 null）。
- 监听器/abort：`add/remove/removeAll` 各两类监听器；`abort()` 设标志，`clearAbortRequest()` 由具体子类在每轮清零；`processImageStarted/Progress/Complete/readAborted` 由子类调用。
- 静态工具（protected）：`checkReadParamBandSettings`、`computeRegions`、`checkImagesIndexed` 等。

## 2. Java 用户如何使用
`ImageInputStream in = ImageIO.createImageInputStream(file); reader.setInput(in, true, true); BufferedImage bi = reader.read(0, reader.getDefaultReadParam()); reader.dispose();`。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/ImageReader.java`：`setInput(null)` 不重置 seekForwardOnly/ignoreMetadata 标志（与 javadoc 语义分歧）；`getNumImages(true)` 在 seekForwardOnly 时抛 `IllegalStateException("seekForwardOnly and allowSearch can't both be true!")`。
- `com/sun/imageio/plugins/png/PNGImageReader.java`：`read()` 起手 `checkAbortRequested()`、`processImageStarted`、`processImageProgress`、完成时 `processImageComplete`；abort 时 `processReadAborted` 且不再发 Complete。
- `test/jdk/javax/imageio/AbortController.java`（EventQueue 驱动 abort）与 `ReadAbort.java` 覆盖 abort 协议。
- `IIOServiceProvider.getInputTypes()`：reader SPI 的 inputTypes 限定合法输入类型集合。

## 4. 测试用例设计 + 自动化实现（`ImageReaderTest`，双轨：真实 PNG 插件 + 自写 StubReader）
- 白盒：全部方法/字段清单存在性（保留原有 57 条）。
- 真实 PNG 轨：无输入 getWidth/getHeight/getNumImages/read/getImageMetadata 等 ISE；越界 index IOOBE；`setInput(null)` 保留标志怪癖；metadata format 分派与未知 format null；raster/tile/thumbnail 的 UOE/IAE；null 监听器全 no-op；reset 清状态；seekForwardOnly+allowSearch ISE；read 返回正确尺寸图像。
- StubReader 轨（`StubReaderSpi`+`StubReader`）：getFormatName 无 provider NPE；provider 决定 inputTypes（File 也可被接受）；`StubMetadata` 验证 std/native/extra format 返回同一实例、bogus 返回 null。
- 监听器与 abort：重复注册两次只通知一次计数、remove 生效、abort-from-listener → readAborted 发生且无 imageComplete；陈旧 abort 标志在下一次 process 周期被清。
- readAll 双路径：`readAll(0,null)` 直接分派；Iterator 形式发 sequenceStarted/Complete 且逐元素校验类型（非 ImageReadParam 元素 IAE）；`forceMinIndex` 后 index<minIndex 抛 IOOBE。
- 静态工具：`checkReadParamBandSettings`（数量不匹配 IAE）与 `computeRegions`（region 裁剪 + destinationOffset 平移）。
