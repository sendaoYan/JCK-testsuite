# java.awt.image.RGBImageFilter 设计案

## 1. API 功能分组
- 抽象方法：`filterRGB(int x, int y, int rgb)`。
- 重写：`setColorModel/substituteColorModel/filterIndexColorModel/filterRGBPixels/setPixels(...byte[]...)/setPixels(...int[]...)`。
- 继承：ImageFilter 的透传方法。

## 2. Java 用户用法
- 逐像素 RGB 变换：子类实现 `filterRGB` 返回新 ARGB；`new FilteredImageSource(src, filter)` 再 `PixelGrabber`。
- 对 `IndexColorModel` 源会调用 `filterIndexColorModel` 一次性变换调色板。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 `java.awt.image.BufferedImageFilter` 等使用 RGBImageFilter。
- `test/jdk/java/awt/image/FilteredImageSourceTest.java` 覆盖 RGB 过滤。
- 图像库用 RGBImageFilter 做透明度/通道交换/反色。

## 4. 测试设计（指向 RGBImageFilterTest.java）
- 白盒：abstract、extends ImageFilter、filterRGB 抽象与 setColorModel 等方法存在。
- 黑盒：强制 alpha 子类把半透明像素变 opaque 且保留 RGB；常量子类全像素变绿；`filterIndexColorModel` 返回新 IndexColorModel。
