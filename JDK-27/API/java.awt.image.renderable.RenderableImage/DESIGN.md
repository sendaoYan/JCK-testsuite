# java.awt.image.renderable.RenderableImage 设计案

## 1. API 功能分组
- **类型**：interface。
- **常量**：`HINTS_OBSERVED`（"HINTS_OBSERVED"）。
- **几何**：`getWidth/getHeight/getMinX/getMinY`（用户坐标）。
- **源/属性**：`getSources/getProperty/getPropertyNames/isDynamic`。
- **渲染**：`createScaledRendering(int,int,RenderingHints)`、`createDefaultRendering()`、`createRendering(RenderContext)`。

## 2. Java 用户如何使用
分辨率无关的可渲染图像：`createDefaultRendering()` 按默认尺寸渲染、`createScaledRendering(w,h,hints)` 缩放渲染、`createRendering(rc)` 按上下文渲染。

## 3. 开源代码实际用法调研
- `java.awt.image.renderable.RenderableImage` 是可渲染图像接口，`RenderableImageOp` 实现（`src/java.desktop/share/classes/java/awt/image/renderable/`）。
- `test/jdk/java/awt/image/renderable/` 用例验证 createRendering 链（本用例接口契约参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`RenderableImageTest.testBlackBox()`，匿名实现）：
1. HINTS_OBSERVED 常量值；几何 getWidth/getHeight/getMinX/getMinY。
2. getSources/getProperty(UndefinedProperty)/isDynamic。
3. createDefaultRendering/createScaledRendering/createRendering 返回渲染图像。
白盒（`testWhiteBox()`）：interface、HINTS_OBSERVED 常量、3 个渲染方法签名。
