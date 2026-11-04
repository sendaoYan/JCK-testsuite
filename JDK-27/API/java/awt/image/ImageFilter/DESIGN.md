# java.awt.image.ImageFilter 设计案

## 1. API 功能分组
- 构造：`ImageFilter()`。
- 透传/派生：`getFilterInstance(ImageConsumer)`、`clone()`。
- ImageConsumer 透传：`setDimensions/setProperties/setColorModel/setHints/setPixels(byte[])/setPixels(int[])/imageComplete`。
- 重发：`resendTopDownLeftRight(ImageProducer)`。

## 2. Java 用户用法
- 作为图像过滤器基类；`getFilterInstance(consumer)` 返回绑定目标消费者的克隆实例，随后由 producer 驱动 `setDimensions/setPixels/imageComplete`。
- 默认实现逐项透传到下游 `ImageConsumer`。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `ReplicateScaleFilter/CropImageFilter/RGBImageFilter` 均继承 ImageFilter。
- `test/jdk/java/awt/image/FilteredImageSourceTest.java` 覆盖过滤器链。
- JDK 内部 `FilteredImageSource` 用 `getFilterInstance` 为每个消费者克隆过滤器。

## 4. 测试设计（指向 ImageFilterTest.java）
- 白盒：implements ImageConsumer/Cloneable、透传与派生方法存在。
- 黑盒：`getFilterInstance` 返回新实例并透传 `setDimensions/setColorModel/setHints/setPixels/imageComplete/setProperties`；`clone` 独立性；`resendTopDownLeftRight` 可调用。
