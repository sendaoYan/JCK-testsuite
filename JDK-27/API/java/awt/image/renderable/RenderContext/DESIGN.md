# java.awt.image.renderable.RenderContext 设计案

## 1. API 功能分组
- **类型**：RenderContext implements Cloneable。
- **构造**：`(AffineTransform)`、`(AffineTransform, RenderingHints)`、`(AffineTransform, Shape)`、`(AffineTransform, Shape, RenderingHints)`。
- **变换**：`getTransform/setTransform/preConcatenateTransform/preConcetenateTransform/concatenateTransform/concetenateTransform`。
- **属性**：`getRenderingHints/setRenderingHints`、`getAreaOfInterest/setAreaOfInterest`；`clone()`。

## 2. Java 用户如何使用
描述可渲染图像的渲染上下文（用户坐标变换 + 兴趣区域 + 渲染提示）；`RenderableImage.createRendering(RenderContext)` 据此渲染。

## 3. 开源代码实际用法调研
- `java.awt.image.renderable.RenderContext` 是渲染上下文封装（`src/java.desktop/share/classes/java/awt/image/renderable/RenderContext.java`）。
- `test/jdk/java/awt/image/renderable/` 用例构造 RenderContext 渲染图像（本用例变换/克隆参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`RenderContextTest.testBlackBox()`）：
1. 构造 transform/AOI/hints；getTransform 防御性副本。
2. setTransform/setAreaOfInterest/setRenderingHints；preConcatenateTransform/concatenateTransform；clone 独立相等。
白盒（`testWhiteBox()`）：public、implements Cloneable。
