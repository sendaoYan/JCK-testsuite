# java.awt.font.ImageGraphicAttribute 设计案

## 1. API 功能分组
- **构造**：`ImageGraphicAttribute(Image, int alignment)`、`ImageGraphicAttribute(Image, int alignment, float originX, float originY)`。
- **度量**：`getAdvance()`（= 图宽）、`getAscent/getDescent`（由 alignment 与图高推出）、`getBounds()`。
- **绘制**：`draw(Graphics2D, float x, float y)`。
- **工具**：`hashCode/equals`。

## 2. Java 用户如何使用
把 `java.awt.Image` 作为内联图形嵌入文本：`new ImageGraphicAttribute(image, GraphicAttribute.ROMAN_BASELINE)` 放入 `AttributedString`，`TextLayout` 排版时按 image 宽高推进基线。TOP_ALIGNMENT 时 ascent=0、descent=图高；BOTTOM 反之。

## 3. 开源代码实际用法调研
- `ImageGraphicAttribute` 是 `GraphicAttribute` 内置 final 实现（`src/java.desktop/share/classes/java/awt/font/ImageGraphicAttribute.java`），`getAdvance` 返回 `image.getWidth(null)`。
- `test/jdk/java/awt/font/TextLayout/` 内联图形排版用例以 ImageGraphicAttribute 验证 advance/ascent（本用例度量语义参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ImageGraphicAttributeTest.testBlackBox()`）：
1. (image,TOP_ALIGNMENT) advance==宽、ascent==0/descent==高、bounds 尺寸。
2. BOTTOM_ALIGNMENT 时 ascent/descent 翻转。
3. (image,ROMAN_BASELINE,originX,originY) 构造。
4. equals/hashCode 契约。
白盒（`testWhiteBox()`）：public final、extends GraphicAttribute、度量与 draw 方法签名。
