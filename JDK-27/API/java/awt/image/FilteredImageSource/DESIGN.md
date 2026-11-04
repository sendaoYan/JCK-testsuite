# java.awt.image.FilteredImageSource 设计案

## 1. API 功能分组
- 构造：`FilteredImageSource(ImageProducer orig, ImageFilter imgf)`。
- ImageProducer：`addConsumer/isConsumer/removeConsumer/startProduction/requestTopDownLeftRightResend`。

## 2. Java 用户用法
- 生产者+过滤器组合：`new FilteredImageSource(memorySource, new CropImageFilter(...))` 再 `PixelGrabber`；`startProduction` 让源图像经过滤器推送到消费者。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `test/jdk/java/awt/image/FilteredImageSourceTest.java` 直接测试该组合。
- JDK 内部 `Image` 缩放/裁剪实现基于 FilteredImageSource。
- 图像库用其把任意 ImageProducer 串上 ImageFilter。

## 4. 测试设计（指向 FilteredImageSourceTest.java）
- 白盒：implements ImageProducer、5 方法存在。
- 黑盒：恒等 crop 透传尺寸与像素；1x1 crop 缩小输出；add/isConsumer/removeConsumer 状态；`startProduction` 交付 STATICIMAGEDONE。
