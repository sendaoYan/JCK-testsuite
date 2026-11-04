# java.awt.font.GlyphVector 设计案

## 1. API 功能分组
- **工厂**：由 `Font.createGlyphVector(FontRenderContext, String|char[]|int[])` 构造（GlyphVector 本身抽象）。
- **查询**：`getFont/getFontRenderContext/getNumGlyphs/getGlyphCode(i)/getGlyphCodes/getGlyphCharIndex(i)/getLayoutFlags`。
- **度量**：`getLogicalBounds/getVisualBounds/getPixelBounds/getGlyphMetrics(i)/getGlyphPosition(i)/getGlyphPositions`。
- **轮廓**：`getOutline/getOutline(x,y)/getGlyphOutline(i)/getGlyphOutline(i,x,y)/getGlyphLogicalBounds(i)/getGlyphVisualBounds(i)`。
- **修改**：`setGlyphPosition/setGlyphTransform`；`performDefaultLayout()`。

## 2. Java 用户如何使用
`GlyphVector gv = font.createGlyphVector(frc, "text");` 后遍历 `gv.getNumGlyphs()`、读 `gv.getGlyphMetrics(i).getAdvance()`、取 `gv.getOutline()` 描边绘制；`gv.getGlyphPosition(i)` 用于光标定位。是字形级排版的中间表示。

## 3. 开源代码实际用法调研
- `java.awt.font.TextLayout` 内部持有 GlyphVector 并委托度量与绘制（`src/java.desktop/share/classes/java/awt/font/TextLayout.java`）。
- `Font.createGlyphVector` 是标准构造入口（`src/java.desktop/share/classes/java/awt/Font.java`）。
- `test/jdk/java/awt/font/GlyphVector/` 系列用例遍历 glyph 断言 advance/bounds/outline（本用例度量范式一致）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`GlyphVectorTest.testBlackBox()`，用真实字体 "Hello"）：
1. getFont/getFontRenderContext/getNumGlyphs==5。
2. getGlyphCode/getGlyphCodes 一致；getGlyphCharIndex(0)==0。
3. logical/visual bounds、getOutline、getGlyphOutline、getGlyphPosition、getGlyphMetrics advance>0。
4. getLayoutFlags>=0、equals、getPixelBounds 非空。
白盒（`testWhiteBox()`）：abstract public class、implements Cloneable、关键查询方法签名。
