# javax.imageio.metadata.IIOMetadata — DESIGN（JDK-8）

## 1. API 功能
`IIOMetadata` 是表示图像/流元数据（非图像数据）的抽象基类，插件用它暴露元数据的 DOM 树视图与格式描述。方法分组：
- 格式发现：`isStandardMetadataFormatSupported()`、`getNativeMetadataFormatName()`、`getExtraMetadataFormatNames()`、`getMetadataFormatNames()`（组合顺序：native → standard → extra，全无则返回 null）、`getMetadataFormat(String)`（null/未知名抛 `IllegalArgumentException`；类加载失败抛 `IllegalStateException`）。
- 树读写：抽象 `getAsTree(String)`、抽象 `mergeTree(String, Node)`（抛 `IIOInvalidTreeException`）、`setFromTree(String, Node)`（默认实现 = `reset()` + `mergeTree`）、抽象 `reset()`、`isReadOnly()`。
- 控制器：`setController/getController/getDefaultController/hasController/activateController`（无控制器 `activateController` 抛 `IllegalStateException`，否则 `getController().activate(this)`）。
- 唯一构造器为 protected：`IIOMetadata(boolean, String, String, String[], String[])`，校验 extra 名/类数组的四种不匹配均抛 `IllegalArgumentException`。
- 受保护状态字段：`standardFormatSupported`、`nativeMetadataFormatName`、`nativeMetadataFormatClassName`、`extraMetadataFormatNames`、`extraMetadataFormatClassNames`、`defaultController`、`controller`。

## 2. Java 用户如何使用
用户不直接 `new IIOMetadata`，而是从 `ImageWriter.getDefaultImageMetadata(...)` / `ImageReader.getImageMetadata(int)` 拿到插件实现：
`ImageWriter w = ImageIO.getImageWritersByFormatName("png").next(); IIOMetadata md = w.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(bi), null);`
典型链：`getMetadataFormatNames()` → `getMetadataFormat("javax_imageio_1.0")` → `getAsTree(...)` → 修改树 → `mergeTree/setFromTree(...)`；控制器链：`setController(c)` → `activateController()`。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + jdk/test，只借鉴思路）
- `jdk/src/share/classes/javax/imageio/metadata/IIOMetadata.java`：`getMetadataFormatNames()` 组合 native + 标准名 + extra 名；`setFromTree` 默认实现为 `reset()` 后 `mergeTree(...)`；`activateController()` 无控制器时抛 `IllegalStateException`；`getMetadataFormat` 通过反射调用格式类的静态 `getInstance`，失败包装成 `IllegalStateException`。
- `jdk/src/share/classes/com/sun/imageio/plugins/png/PNGMetadata.java`：PNG 插件实现；`mergeTree` 对未识别格式名抛 `IllegalArgumentException("Not a recognized format!")`，格式名为 null 时是 `String.equals` 触发的 `NullPointerException`；根节点名错误抛 `IIOInvalidTreeException` 且 `getOffendingNode()` 指向坏根；未知子元素被静默忽略（不抛异常）；`mergeTree(fmt, null)` 抛 `IllegalArgumentException("root == null!")`。
- `jdk/test/javax/imageio/metadata/IIOMetadataFormat/MetadataTest.java`：插件用 `getMetadataFormat(...)` 通过 `getInstance` 反射加载格式实现。
- `jdk/test/javax/imageio/metadata/RegisteredFormatsTest.java`：遍历全部注册插件的元数据格式做一致性检查。

## 4. 测试用例设计 + 易错规范细节（`IIOMetadataTest`）
- 白盒：abstract/public/extends Object、4 个抽象方法、protected 构造器、15 个方法名齐全。
- PNG writer 默认元数据：native 名 `javax_imageio_png_1.0`、standard 支持、extra null、`getMetadataFormatNames` 顺序 [native, standard]、standard 描述符是 `IIOMetadataFormatImpl.getStandardFormatInstance()` 同一单例。
- 标准元数据链路（真实 round-trip）：`ImageIO.write` 8x8 TYPE_INT_RGB PNG 到内存 → `ImageIO.getImageReadersByFormatName("png")` → `getImageMetadata(0)` → `getAsTree("javax_imageio_1.0")` 断言根名与 5 个顶层子树（Chroma/Compression/Data/Dimension/Transparency）及确定性属性值（ColorSpaceType=RGB、NumChannels=3、BlackIsZero=TRUE、CompressionTypeName=deflate、Lossless=TRUE、SampleFormat=UnsignedIntegral、BitsPerSample="8 8 8"、PixelAspectRatio=1.0、ImageOrientation=Normal、Alpha=none、无 Palette）。
- mergeTree/setFromTree 负例（实测精确类）：未知格式 IAE、null 格式 NPE（PNG 实现先 `equals` 格式名）、null 根 IAE、错误根名 `IIOInvalidTreeException` 且 `getOffendingNode()` 身份正确（standard 与 native 两路）、未知子元素不抛。
- merge→getAsTree 往返：构造 Dimension/PixelAspectRatio=2.0 树 merge 后能读回 2.0；native 树自合并；setFromTree(std) 往返。
- 子类探针：protected 构造器 4 种 IAE；无任何格式时 `getMetadataFormatNames()` null、standard 不支持时 `getMetadataFormat(std)` IAE；extra 通道 `getMetadataFormat("good")` 经静态 `getInstance` 加载同一实例（名序 [standard, extra]）；类名不可加载 → `IllegalStateException`；`setFromTree` 默认实现先 reset 后 mergeTree（计数与参数身份验证）；子类覆写 `getAsTree` 返回自建树。
- 控制器：default null、`setController(null)` 合法、无控制器 activateController ISE、回调收到 `this`（身份断言）、false/true 透传、清除后 ISE。
- 易错点：不要断言 `getAsTree(null)` 抛 IAE——PNG 实现实测是 NPE；`getMetadataFormat` 对 null 抛 IAE（`"formatName == null!"`）与 `getAsTree(null)` 的 NPE 不同源。
