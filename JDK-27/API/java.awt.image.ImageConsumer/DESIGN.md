# java.awt.image.ImageConsumer 设计案

## 1. API 功能分组
- 常量：`RANDOMPIXELORDER/TOPDOWNLEFTRIGHT/COMPLETESCANLINES/SINGLEPASS/SINGLEFRAME`（hints）、`IMAGEERROR/SINGLEFRAMEDONE/STATICIMAGEDONE/IMAGEABORTED`（imageComplete 状态）。
- 方法：`setDimensions/setProperties/setColorModel/setHints/setPixels(byte[])/setPixels(int[])/imageComplete`。

## 2. Java 用户用法
- 图像数据接收方抽象；producer 依次调用 `setDimensions→setProperties→setColorModel→setHints→setPixels→imageComplete`。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `PixelGrabber/ImageFilter` 实现本接口。
- `test/jdk/java/awt/image/FilteredImageSourceTest.java` 通过自定义 ImageConsumer 捕获像素。
- JDK 内部 AWT 图像后台消费者订阅 ImageProducer。

## 4. 测试设计（指向 ImageConsumerTest.java）
- 白盒：interface、7 方法签名存在。
- 黑盒：9 个常量精确值（含 hints 与 imageComplete 状态位）。
