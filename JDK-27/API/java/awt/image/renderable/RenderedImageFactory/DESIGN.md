# java.awt.image.renderable.RenderedImageFactory 设计案

## 1. API 功能分组
- **类型**：interface。
- **方法**：`create(ParameterBlock, RenderingHints)`。

## 2. Java 用户如何使用
按参数块与渲染提示产生 RenderedImage；`ContextualRenderedImageFactory` 扩展该接口加入渲染上下文感知。

## 3. 开源代码实际用法调研
- `java.awt.image.renderable.RenderedImageFactory` 是渲染工厂基接口（`src/java.desktop/share/classes/java/awt/image/renderable/RenderedImageFactory.java`）。
- `test/jdk/java/awt/image/renderable/` 用例以匿名工厂验证 create（本用例接口契约参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`RenderedImageFactoryTest.testBlackBox()`，匿名实现）：
1. create(ParameterBlock, RenderingHints) 返回 6x6 图像。
白盒（`testWhiteBox()`）：interface、create 签名。
