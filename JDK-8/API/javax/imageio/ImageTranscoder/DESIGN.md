# javax.imageio.ImageTranscoder — DESIGN（JDK-8）

## 1. API 功能
`ImageTranscoder` 是无父接口的公共接口，恰好两个方法：
- `IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param)`
- `IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier type, ImageWriteParam param)`
用于把 reader 读出的元数据转成 writer 可写的元数据。平台内唯一实现者是 `ImageWriter implements ImageTranscoder`。

## 2. Java 用户如何使用
把 writer 当 transcoder 用：`ImageWriter w = ImageIO.getImageWritersByFormatName("png").next(); IIOMetadata m = w.convertImageMetadata(reader.getImageMetadata(0), its, w.getDefaultWriteParam());` 之后 `w.write(streamMeta, new IIOImage(img, null, m), param)`。跨格式转码（PNG→JPEG 元数据）同理。

## 3. 开源代码实际用法调研（来源：~/git/jdk 系源码与 test/jdk，仅借鉴思路）
- `src/java.desktop/share/classes/javax/imageio/ImageWriter.java`：`public abstract class ImageWriter implements ImageTranscoder`。
- `src/java.desktop/share/classes/javax/imageio/ImageIO.java`：`getImageTranscoders` 经 `ImageTranscoderSpi` 类别过滤；内置注册表无独立 transcoder 插件 → 返回空迭代器。
- `test/jdk/javax/imageio/PNGSpiStreamMetadata.java`：PNG 无流元数据语义。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/png/PNGImageWriter.java`：`convertStreamMetadata` 无条件返回 null（PNG 无流元数据）；`convertImageMetadata` 对 null inData 退回默认元数据。

## 4. 测试用例设计 + 易错规范细节（`ImageTranscoderTest`）
- 接口形状（反射）：恰好 2 方法、均 public abstract、返回 IIOMetadata、`convertImageMetadata` 3 参数；无字段常量；`ImageWriter` 实现 / `ImageReader` 不实现；writer 可经接口类型使用。
- PNG 端到端链（实测）：write→read 的 `getImageMetadata(0)` 非 null、`isStandardMetadataFormatSupported` true、格式名含 `javax_imageio_png_1.0` 与 `javax_imageio_1.0`、标准树根节点名 `javax_imageio_1.0`、`getStreamMetadata()` 为 null；`convertStreamMetadata(null/流元数据, param)` → null；`getDefaultStreamMetadata(null)` → null；`convertImageMetadata(pngMeta,...)` 返回**新对象**（非同引用）、非只读、保留原生格式名；`convertImageMetadata(null, its, param)` 非 null；`getDefaultImageMetadata(its,null)` 非 null 且含 PNG 原生格式。
- 内置 writer 的 null 语义（全部实测）：BMP `convertStreamMetadata(null,null)`/`convertImageMetadata(null,its,null)` 均返回 null；**GIF 两方法对 null inData 抛 `IllegalArgumentException("inData == null!")`**（违反 javadoc "null → 返回默认"的措辞，实测记录）；JPEG `convertStreamMetadata(null,null)` → null 但 `convertImageMetadata(null,its,null)` 抛 `NullPointerException`；JPEG 接 PNG 元数据跨格式转换非 null。
- 查找：`ImageIO.getImageTranscoders(pngReader, pngWriter)` 为空迭代器；null reader/writer → IAE。
- 易错点：断言 GIF/JPEG 的 null 行为时必须实测精确类（GIF=IAE、JPEG=NPE，两侧行为一致）；`convertImageMetadata` 可能返回与输入相同的实现类型但是新实例，勿断言同引用。
