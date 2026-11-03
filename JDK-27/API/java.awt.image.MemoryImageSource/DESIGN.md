# java.awt.image.MemoryImageSource 设计案

## 1. API 功能分组
- 构造：int[]/byte[] + ColorModel 的多重载。
- ImageProducer：`addConsumer/isConsumer/removeConsumer/startProduction/requestTopDownLeftRightResend`。
- 动态更新：`setAnimated/setFullBufferUpdates/newPixels()`、`newPixels(x,y,w,h[,boolean])`、`newPixels(byte[]/int[], ColorModel, off, scan)`。

## 2. Java 用户用法
- 内存像素源：`new MemoryImageSource(w, h, ints, 0, w)` 再 `new PixelGrabber(src,...).grabPixels()`。
- 动画/更新用 `newPixels(...)` 通知消费者重新取像素。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `test/jdk/java/awt/image/FilteredImageSourceTest.java` 用 MemoryImageSource 构造源图像。
- JDK 内部图像解码与测试大量用 `new MemoryImageSource(...)` 生成内存图像。
- 图像库用其直接构造 `Image` 对象。

## 4. 测试设计（指向 MemoryImageSourceTest.java）
- 白盒：implements ImageProducer、newPixels/setAnimated 等方法存在。
- 黑盒：add/isConsumer/removeConsumer 状态；`startProduction` 交付尺寸/像素/STATICIMAGEDONE/颜色模型；动画标志与 `newPixels` 各重载可调用。
