# java.awt.image.renderable.RenderableImageProducer 设计案

## 1. API 功能分组
- **类型**：RenderableImageProducer implements ImageProducer、Runnable。
- **构造**：`RenderableImageProducer(RenderableImage, RenderContext)`。
- **上下文**：`setRenderContext(RenderContext)`。
- **消费者**：`addConsumer/isConsumer/removeConsumer/startProduction/requestTopDownLeftRightResend`。
- **执行**：`run()`（渲染并推送像素）。

## 2. Java 用户如何使用
把 RenderableImage 适配为 ImageProducer，`addConsumer(ic)` 后由消费者接收渲染像素；`setRenderContext` 指定渲染上下文。

## 3. 开源代码实际用法调研
- `java.awt.image.renderable.RenderableImageProducer` 把 RenderableImage 包装为 ImageProducer（`src/java.desktop/share/classes/java/awt/image/renderable/RenderableImageProducer.java`）。
- `test/jdk/java/awt/image/renderable/` 用例经生产者消费渲染结果（本用例消费者管理参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`RenderableImageProducerTest.testBlackBox()`，匿名 RenderableImage）：
1. setRenderContext；addConsumer/isConsumer/removeConsumer 状态流。
白盒（`testWhiteBox()`）：public、implements ImageProducer 与 Runnable。
