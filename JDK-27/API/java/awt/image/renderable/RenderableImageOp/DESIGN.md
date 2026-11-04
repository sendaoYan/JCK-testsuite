# java.awt.image.renderable.RenderableImageOp 设计案

## 1. API 功能分组
- **类型**：RenderableImageOp implements RenderableImage。
- **构造**：`RenderableImageOp(ContextualRenderedImageFactory, ParameterBlock)`。
- **几何**：`getWidth/getHeight/getMinX/getMinY`（委托 CRIF.getBounds2D）。
- **源/属性**：`getSources/getProperty/getPropertyNames/isDynamic`。
- **参数块**：`setParameterBlock/getParameterBlock`。
- **渲染**：`createScaledRendering/createDefaultRendering/createRendering`。

## 2. Java 用户如何使用
把 CRIF + ParameterBlock 包装成 RenderableImage；`createDefaultRendering()` 经 CRIF 产生最终 RenderedImage，`setParameterBlock` 动态改操作参数。

## 3. 开源代码实际用法调研
- `java.awt.image.renderable.RenderableImageOp` 的 getWidth/getHeight/getMinX/getMinY 委托 `myCRIF.getBounds2D(paramBlock)`，getProperty/isDynamic 委托 CRIF（`src/java.desktop/share/classes/java/awt/image/renderable/RenderableImageOp.java`）。
- `test/jdk/java/awt/image/renderable/` 用例验证委托语义（本用例 bounds 委托参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`RenderableImageOpTest.testBlackBox()`，匿名 CRIF 返回 100x50 bounds）：
1. getWidth==100/getHeight==50/getMinX==getMinY==0/isDynamic/getPropertyNames/getProperty。
2. setParameterBlock 返回旧块、getParameterBlock 取新块。
3. createDefaultRendering/createScaledRendering/createRendering 经 CRIF 渲染。
白盒（`testWhiteBox()`）：public、implements RenderableImage。
