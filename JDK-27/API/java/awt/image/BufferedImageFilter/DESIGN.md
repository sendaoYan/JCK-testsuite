# java.awt.image.BufferedImageFilter 设计案

## 1. API 功能分组
- **类型**：BufferedImageFilter extends ImageFilter、implements Cloneable。
- **构造**：`BufferedImageFilter(BufferedImageOp)`。
- **查询**：`getBufferedImageOp()`。
- **生产/消费**：继承 ImageFilter 的 `setDimensions/setColorModel/setPixels/imageComplete`，把像素组装成 BufferedImage 后经 op 过滤输出。

## 2. Java 用户如何使用
`new BufferedImageFilter(new RescaleOp(1.2f, 10f, null))` 作为 ImageFilter 链，配合 ImageProducer 对整幅图做逐像素运算。

## 3. 开源代码实际用法调研
- `java.awt.image.BufferedImageFilter` 是 ImageFilter 子类，把流式像素喂给 BufferedImageOp（`src/java.desktop/share/classes/java/awt/image/BufferedImageFilter.java`）。
- `test/jdk/java/awt/image/` 图像过滤链用例验证过滤输出（本用例 imageComplete 管线参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`BufferedImageFilterTest.testBlackBox()`）：
1. getBufferedImageOp 返回构造 op。
2. 经 addConsumer + setDimensions/setColorModel/setPixels/imageComplete 管线，消费者收到 imageComplete。
白盒（`testWhiteBox()`）：public、extends ImageFilter、implements Cloneable。
