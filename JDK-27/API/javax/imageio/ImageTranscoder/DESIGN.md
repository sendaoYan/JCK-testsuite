# javax.imageio.ImageTranscoder — DESIGN

## 1. API 功能
`ImageTranscoder` 是接口，声明两个元数据转换方法：
- `IIOMetadata convertStreamMetadata(IIOMetadata, ImageWriteParam)`。
- `IIOMetadata convertImageMetadata(IIOMetadata, ImageTypeSpecifier, ImageWriteParam)`。

用于在不同格式 reader/writer 之间转换元数据。`ImageWriter` 实现该接口。

## 2. Java 用户如何使用
通过 `ImageIO.getImageTranscoders(reader, writer)` 获取可用转换器，或把 `ImageWriter` 当作 `ImageTranscoder`：`ImageWriter w = ...; IIOMetadata m = w.convertImageMetadata(srcMeta, its, w.getDefaultWriteParam());`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/ImageWriter.java`：`public abstract class ImageWriter implements ImageTranscoder`，其 `write` 方法依赖转换后的元数据。
- `src/java.desktop/share/classes/javax/imageio/ImageIO.java:1201`：`getImageTranscoders` 通过 `ImageTranscoderSpi` 过滤返回迭代器，缺省类别时返回空迭代器。

## 4. 测试用例设计 + 自动化实现（`ImageTranscoderTest.testBlackBox`）
- `ImageIO.getImageTranscoders(pngReader,pngWriter)` 非 null（可为空）。
- `ImageWriter instanceof ImageTranscoder` 成立，把 writer 当转换器用。
- `convertStreamMetadata(null,null)` 返回 null（PNG 无流元数据）。
- `convertImageMetadata(null, its, null)` 返回非 null 元数据对象。
- 白盒：isInterface、无父接口、两个公共方法。
