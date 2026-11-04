# java.awt.font.ShapeGraphicAttribute 设计案

## 1. API 功能分组
- **常量**（boolean）：`STROKE=true`、`FILL=false`。
- **构造**：`ShapeGraphicAttribute(Shape, int alignment, boolean stroke)`。
- **度量**：`getAdvance()`（= shape 宽）、`getAscent/getDescent`（由 alignment 与 shape 高推出）、`getBounds()`、`getOutline(AffineTransform)`。
- **绘制**：`draw(Graphics2D, float, float)`；`hashCode/equals`。

## 2. Java 用户如何使用
把几何 `Shape` 作为内联图形嵌入文本：`new ShapeGraphicAttribute(rect, GraphicAttribute.ROMAN_BASELINE, ShapeGraphicAttribute.STROKE)` 放入 AttributedString，TextLayout 排版时按 shape 边界推进基线；STROKE 表示描边、FILL 表示填充。

## 3. 开源代码实际用法调研
- `ShapeGraphicAttribute` 是 `GraphicAttribute` 内置 final 实现（`src/java.desktop/share/classes/java/awt/font/ShapeGraphicAttribute.java`），`getAdvance` 返回 shape bounds 宽度。
- `test/jdk/java/awt/font/TextLayout/` 内联图形排版用例以 ShapeGraphicAttribute 验证 advance/ascent（本用例度量语义参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ShapeGraphicAttributeTest.testBlackBox()`）：
1. STROKE/FILL 常量值。
2. 矩形 shape 的 advance==宽、ROMAN_BASELINE ascent/descent、bounds 尺寸、getOutline 非空。
3. TOP_ALIGNMENT 度量；equals/hashCode 契约（stroke/fill 差异）。
白盒（`testWhiteBox()`）：public final、extends GraphicAttribute、STROKE/FILL 常量、度量与 getOutline 签名。
