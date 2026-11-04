# javax.imageio.IIOImage — DESIGN

## 1. API 功能
`IIOImage` 是读写插件之间传递图像的容器：可同时携带一个 `RenderedImage` 或一个 `Raster`、一个缩略图列表 `List<? extends BufferedImage>` 和一个 `IIOMetadata`。方法分组：
- 构造：`IIOImage(RenderedImage, List, IIOMetadata)`、`IIOImage(Raster, List, IIOMetadata)`。
- 主图：`getRenderedImage`/`setRenderedImage`/`hasRaster`/`getRaster`/`setRaster`（set 一方会丢弃另一方）。
- 缩略图：`getNumThumbnails`/`getThumbnail(int)`/`getThumbnails`/`setThumbnails`。
- 元数据：`getMetadata`/`setMetadata`。

## 2. Java 用户如何使用
典型链：`new IIOImage(bufferedImage, thumbnails, metadata)` → `ImageWriter.write(iioImage)`；或用 Raster 变体写栅格数据。读取侧 `ImageReader.readAll(...)` 返回 `IIOImage` 供用户取 `getRenderedImage()`/`getNumThumbnails()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/ImageWriter.java:594`：`write(RenderedImage image)` 实现为 `write(null, new IIOImage(image, null, null), null)`（源码内）。
- `src/java.desktop/share/classes/javax/imageio/ImageWriter.java:612`：`write(IIOImage image)` 简化为 `write(null, image, null)`。
- `test/jdk/javax/imageio/ImageReaderReadAll.java`：`IIOImage image; image = ireader.readAll(0, irp); image.getRenderedImage()` 的组合用法。

## 4. 测试用例设计 + 自动化实现（`IIOImageTest.testBlackBox`）
- RenderedImage 构造：image 身份、hasRaster=false、getRaster=null、thumbnails/metadata 初始 null。
- Raster 构造：hasRaster=true、getRaster 身份、getRenderedImage=null。
- setRenderedImage 丢弃 raster / setRaster 丢弃 image。
- 缩略图列表组合：setThumbnails 存活引用、getNumThumbnails、getThumbnail 索引、null 清空。
- 异常：两种构造器 null 抛 IAE、setRenderedImage/setRaster null 抛 IAE、空缩略图 getThumbnail 抛 IndexOutOfBoundsException。
- 白盒：两个构造器 + 四个 protected 字段类型。
