# java.awt.image.CropImageFilter 设计案

## 1. API 功能分组
- 构造：`CropImageFilter(int x, int y, int w, int h)`。
- 重写：`setProperties/setDimensions/setPixels(...byte[]...)/setPixels(...int[]...)`。

## 2. Java 用户用法
- 裁剪：`new FilteredImageSource(src, new CropImageFilter(x,y,w,h))` 再 `PixelGrabber`；输出尺寸为裁剪矩形 `(w,h)`，像素从源矩形截取并偏移到 (0,0)。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 `Image.getSubimage` 与图像裁剪逻辑使用 `CropImageFilter`。
- `test/jdk/java/awt/image/FilteredImageSourceTest.java` 覆盖裁剪过滤器。
- 图像库用 CropImageFilter 截取子区域。

## 4. 测试设计（指向 CropImageFilterTest.java）
- 白盒：extends ImageFilter、setDimensions/setPixels 存在。
- 黑盒：手动驱动 `setDimensions` 报告裁剪尺寸；全链路 `MemoryImageSource→FilteredImageSource(crop)→PixelGrabber` 断言裁剪出的 2x2 像素与状态 ALLBITS。
