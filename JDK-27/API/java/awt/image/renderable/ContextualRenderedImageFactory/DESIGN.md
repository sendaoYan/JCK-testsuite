# java.awt.image.renderable.ContextualRenderedImageFactory 设计案

## 1. API 功能分组
- **类型**：interface extends RenderedImageFactory。
- **上下文映射**：`mapRenderContext(int, RenderContext, ParameterBlock, RenderableImage)`。
- **渲染**：`create(RenderContext, ParameterBlock)`、`create(ParameterBlock, RenderingHints)`（继承）。
- **查询**：`getBounds2D(ParameterBlock)`、`getProperty(ParameterBlock, String)`、`getPropertyNames()`、`isDynamic()`。

## 2. Java 用户如何使用
渲染上下文感知的操作工厂；`mapRenderContext` 调整渲染上下文，`create` 按参数块产生 RenderedImage。`RenderableImageOp` 委托 CRIF 渲染。

## 3. 开源代码实际用法调研
- `java.awt.image.renderable.RenderableImageOp` 持有 CRIF 并委托 getBounds2D/getProperty/isDynamic/create（`src/java.desktop/share/classes/java/awt/image/renderable/RenderableImageOp.java`）。
- `test/jdk/java/awt/image/renderable/` 用例以匿名 CRIF 验证渲染链（本用例委托契约参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ContextualRenderedImageFactoryTest.testBlackBox()`，匿名实现）：
1. mapRenderContext 返回上下文；两个 create 重载返回 8x8 图像。
2. getBounds2D/getProperty/getPropertyNames/isDynamic 委托。
白盒（`testWhiteBox()`）：interface、extends RenderedImageFactory、6 方法签名。
