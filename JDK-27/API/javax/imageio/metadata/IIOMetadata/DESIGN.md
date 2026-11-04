# javax.imageio.metadata.IIOMetadata — DESIGN

## 1. API 功能
`IIOMetadata` 是表示图像/流元数据（非图像数据）的抽象基类，插件用它暴露元数据的 DOM 树视图与格式描述。方法分组：
- 格式发现：`isStandardMetadataFormatSupported()`、`getNativeMetadataFormatName()`、`getExtraMetadataFormatNames()`、`getMetadataFormatNames()`、`getMetadataFormat(String)`。
- 树读写：抽象 `getAsTree(String)`、抽象 `mergeTree(String, Node)`、`setFromTree(String, Node)`、抽象 `reset()`、`isReadOnly()`。
- 标准子树工具：受保护 `getStandardChromaNode()` … `getStandardTransparencyNode()`、`getStandardTree()`。
- 控制器：`setController/getController/getDefaultController/hasController/activateController`。
- 受保护状态字段：`standardFormatSupported`、`nativeMetadataFormatName`、`nativeMetadataFormatClassName`、`extraMetadataFormatNames`、`extraMetadataFormatClassNames`、`defaultController`、`controller`。

## 2. Java 用户如何使用
用户不直接 `new IIOMetadata`，而是从 `ImageWriter.getDefaultImageMetadata(...)` 或 `ImageReader.getImageMetadata(int)` 拿到插件实现，然后：
`ImageWriter w = ImageIO.getImageWritersByFormatName("png").next(); IIOMetadata md = w.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(bi), null);`
典型组合链：`getMetadataFormatNames()` → `getMetadataFormat("javax_imageio_1.0")` → `getAsTree("javax_imageio_1.0")` → 改树 → `setFromTree(...)`；控制器链：`setController(c)` → `activateController()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/metadata/IIOMetadata.java:309`：`getMetadataFormatNames()` 组合 native + 标准名 + extra 名；`:742` `setFromTree` 默认实现为 `reset()` 后 `mergeTree(...)`；`:880` `activateController()` 无控制器时抛 `IllegalStateException`。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/png/PNGMetadata.java`：PNG 插件实现，`getAsTree` 对 `javax_imageio_png_1.0` / `javax_imageio_1.0` 返回 `IIOMetadataNode` 树，`mergeTree` 解析失败抛 `IIOInvalidTreeException`。
- `src/java.desktop/share/classes/javax/imageio/ImageWriter.java`：`getDefaultImageMetadata` 返回该 writer 的默认 `IIOMetadata`。
- `test/jdk/javax/imageio/metadata/IIOMetadataFormat/MetadataTest.java`：插件用 `getMetadataFormat(...)` 通过 `getInstance` 反射加载格式实现。

## 4. 测试用例设计 + 自动化实现（`IIOMetadataTest.testBlackBox`）
- PNG writer 默认元数据：native 名非空、standard 支持、extra==null、`getMetadataFormatNames` 含 native + `javax_imageio_1.0`。
- 格式描述符：`getMetadataFormat` 对 standard/native 均非空且 rootName 正确。
- 树读写：`getAsTree` standard/native 非空、root 名正确；`setFromTree` 与 `getAsTree` 往返后仍非空。
- 状态：`isReadOnly()==false`、`reset()` 可调用。
- 控制器生命周期：初始 null、`setController(null)`、无控制器 `activateController` 抛 `IllegalStateException`、false/true 控制器往返、清除。
- ImageReader 路径：写内存 PNG → 读 `getImageMetadata(0)` 非空且 native/standard 名可用。
