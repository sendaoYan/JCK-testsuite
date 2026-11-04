# javax.imageio.IIOImage — DESIGN（JDK-8）

## 1. API 功能
`IIOImage` 是 reader/writer 之间传递图像的容器：携带一个 `RenderedImage` **或** 一个 `Raster`（互斥）、一个缩略图 `List<? extends BufferedImage>` 和一个 `IIOMetadata`。方法：
- 构造：`IIOImage(RenderedImage, List, IIOMetadata)`、`IIOImage(Raster, List, IIOMetadata)`；主对象 null → `IllegalArgumentException`。
- 主图：`getRenderedImage/setRenderedImage/hasRaster/getRaster/setRaster`（set 一方丢弃另一方，均不接受 null）。
- 缩略图：`getNumThumbnails/getThumbnail(int)/getThumbnails/setThumbnails`。
- 元数据：`getMetadata/setMetadata`（null 容忍）。
四个字段（image/raster/thumbnails/metadata）为 protected。

## 2. Java 用户如何使用
写侧：`writer.write(null, new IIOImage(bufferedImage, thumbnails, streamMetadata), param)`；`ImageWriter.write(RenderedImage)` 内部就是包一个 `IIOImage`。读侧：`ImageReader.readAll(index, param)` 返回 `IIOImage`，再取 `getRenderedImage()`/`getNumThumbnails()`。

## 3. 开源代码实际用法调研（来源：~/git/jdk 系源码与 test/jdk，仅借鉴思路）
- `src/java.desktop/share/classes/javax/imageio/ImageWriter.java`：`write(RenderedImage)` = `write(null, new IIOImage(image, null, null), null)`；`write(IIOImage)` = `write(null, image, null)`。
- `test/jdk/javax/imageio/IIOImageConstructor.java`：两个构造器的 null 参数行为（IAE）。
- `test/jdk/javax/imageio/ImageReaderReadAll.java`：`readAll` 返回 `IIOImage` 后取 `getRenderedImage()` 的组合用法。

## 4. 测试用例设计 + 易错规范细节（`IIOImageTest`）
- thumbnails null 语义（实测）：`getThumbnails()` 返回 **null**（不转空 List）、`getNumThumbnails()==0`、此状态下 `getThumbnail(0)` 抛 `IndexOutOfBoundsException("No thumbnails available!")`。
- 活引用语义（实测）：构造/set 传入的 List **按引用保存**（`getThumbnails()==list`），外部 add/remove 后 `getNumThumbnails`/`getThumbnail(i)` 立即可见——不是快照。空 List 也按引用保存（count 0 但非 null）。
- 越界（实测 JDK-8）：`getThumbnail(size)` → `IndexOutOfBoundsException`；`getThumbnail(-1)` → **ArrayIndexOutOfBoundsException**（JDK-8 的 ArrayList 旧实现；JDK-27 实测为 IndexOutOfBoundsException——真实版本差异，两侧各自断精确类）。
- null 负例（实测）：两个构造器主对象 null、`setRenderedImage(null)`、`setRaster(null)` → `IllegalArgumentException("image == null!"/"raster == null!")`。
- Raster 构造保留 thumbnails/metadata 引用；`setRenderedImage`/`setRaster` 切换时 thumbnails/metadata 不受污染（状态不变量）。
- `hasRaster` 是"哪个槽位被填"的视图：image→false，setRaster→true，再 setRenderedImage→false（非粘性标志）。
- 对象契约：equals **未覆写**（同参两实例 equals false，身份语义）；不实现 Serializable（Raster 不可序列化）；多实例状态独立。
- 易错点：别断言 `getThumbnails()` 非 null（null 保留）；别断言缩略图列表是拷贝（是活引用）。
