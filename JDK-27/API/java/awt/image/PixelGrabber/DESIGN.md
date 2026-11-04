# java.awt.image.PixelGrabber 设计案

## 1. API 功能分组
- 构造：`PixelGrabber(Image,x,y,w,h,pixels[],off,scansize)`、`PixelGrabber(ImageProducer,...)`、`PixelGrabber(Image,x,y,w,h,forceRGB)`。
- 抓取：`grabPixels()`、`grabPixels(long ms)`、`startGrabbing/abortGrabbing`。
- 查询：`getStatus/getWidth/getHeight/getPixels/getColorModel/status`。
- ImageConsumer 实现：`setDimensions/setHints/setProperties/setColorModel/setPixels/imageComplete`。

## 2. Java 用户用法
- 从 Image/ImageProducer 抓像素：`pg.grabPixels()` 返回布尔；`(pg.getStatus() & ImageObserver.ALLBITS) != 0` 表示完成；`getPixels()` 取像素数组。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `test/jdk/java/awt/image/FilteredImageSourceTest.java` 用 PixelGrabber 验证过滤结果。
- JDK 内部测试与图像工具用 PixelGrabber 读取图像像素做断言。
- 图像库用 PixelGrabber 把任意 Image 转成像素数组。

## 4. 测试设计（指向 PixelGrabberTest.java）
- 白盒：implements ImageConsumer、抓取/查询方法存在。
- 黑盒：从 BufferedImage 抓 3x3 像素与状态/尺寸；`getPixels` 为 int[]；从 ImageProducer 抓子区域；forceRGB 构造抓子矩形；`grabPixels(timeout)`；`abortGrabbing` 可调用。
