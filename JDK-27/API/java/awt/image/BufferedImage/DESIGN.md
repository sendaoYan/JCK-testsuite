# java.awt.image.BufferedImage 设计案

## 1. API 功能分组
- 构造/工厂：`BufferedImage(int w,int h,int type)`、带 `IndexColorModel`、`BufferedImage(ColorModel, WritableRaster, boolean, Hashtable)`。
- 像素读写：`setRGB(int,int,int)`、`getRGB(int,int)`、批量 `setRGB/getRGB(int,int,int,int,int[],int,int)`。
- 查询：`getWidth/getHeight/getType/getColorModel/getRaster/getAlphaRaster/getSampleModel/getTransparency/isAlphaPremultiplied/getMinX/getMinY`。
- 图形/子图：`createGraphics/getGraphics/getSubimage/getSource`。
- 瓦片/数据（WritableRenderedImage）：`getTile/getData/copyData/setData/getNumXTiles/getWritableTile` 等。

## 2. Java 用户用法
- 常规链路：`new BufferedImage(w,h,TYPE_INT_ARGB)` → `setRGB(x,y,0xFF112233)` → `getRGB(x,y)` 精确回读；`createGraphics()` 画图后 `dispose()`。
- 批量：`int[] px=...; img.setRGB(0,0,w,h,px,0,w); img.getRGB(...)`。
- 子图：`img.getSubimage(x,y,w,h)` 共享底层数据。
- 瓦片：`img.getTile(0,0)`、`img.getData()` 返回只读 `Raster`。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `~/git/jdk25u-dev/test/jdk/java/awt/image/BufferedImage/` 下有绘制/像素读写用例。
- JDK 内部大量 `new BufferedImage` + `createGraphics` 绘制后编码（`javax.imageio` 系列）。
- `test/jdk/java/awt/image/GetSamplesTest.java`、`GetDataElementsTest.java` 直接对 `BufferedImage.getRaster().getSampleModel()` 采样。
- 像素级处理惯用 `getRGB/setRGB` 或 `getRaster().getSample/setSample`。

## 4. 测试设计（指向 BufferedImageTest.java 黑盒部分）
- 构造 TYPE_INT_ARGB，断言宽高/类型/像素位深/透明度/是否预乘。
- `setRGB/getRGB` 精确 int 回读（含默认 0、批量 4 像素往返）。
- `getRaster`/`getAlphaRaster`/`getSampleModel` 波段数=4。
- `getGraphics/createGraphics/getSubimage/getSource` 非空、子图像素与源一致。
- 瓦片/`getData` 尺寸、`copyData` 采样复制。
- TYPE_INT_RGB 强制 alpha=255；`coerceData(false)` 保持非预乘。
