# java.awt.image.ImageProducer 设计案

## 1. API 功能分组
接口 5 方法：`addConsumer(ImageConsumer)`、`isConsumer(ImageConsumer)`、`removeConsumer(ImageConsumer)`、`startProduction(ImageConsumer)`、`requestTopDownLeftRightResend(ImageConsumer)`。

## 2. Java 用户用法
- 图像数据源抽象；`addConsumer` 注册、`startProduction` 推送像素、`removeConsumer` 注销、`isConsumer` 查询、`requestTopDownLeftRightResend` 请求重发。
- `MemoryImageSource/FilteredImageSource` 是典型实现。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `MemoryImageSource/FilteredImageSource` 实现本接口。
- `test/jdk/java/awt/image/FilteredImageSourceTest.java` 通过生产者-消费者链驱动。
- `PixelGrabber` 作为消费者订阅 ImageProducer。

## 4. 测试设计（指向 ImageProducerTest.java）
- 白盒：interface、5 方法签名存在。
- 黑盒：用 MemoryImageSource 作为实现，断言 add/isConsumer/remove 状态与 `startProduction` 交付尺寸与 STATICIMAGEDONE。
