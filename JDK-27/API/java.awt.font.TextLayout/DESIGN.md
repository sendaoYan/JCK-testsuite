# java.awt.font.TextLayout 设计案

## 1. API 功能分组
- **构造**：`TextLayout(String, Font, FontRenderContext)`、`TextLayout(String, Map<Attribute,?>, FontRenderContext)`、`TextLayout(AttributedCharacterIterator, FontRenderContext)`。
- **度量**：`getAdvance/getVisibleAdvance/getAscent/getDescent/getLeading/getBounds/getPixelBounds/getBaselineOffsets/getCharacterCount/isLeftToRight/isVertical/getBaseline`。
- **命中/光标**：`hitTestChar/hitToPoint/getCaretInfo/getCaretShape/getCaretShapes/getNextRightHit/getNextLeftHit/getVisualOtherHit`。
- **高亮**：`getLogicalHighlightShape/getVisualHighlightShape/getBlackBoxBounds/getLogicalRangesForVisualSelection`。
- **变换/绘制**：`getJustifiedLayout/getOutline/draw/getLayoutPath/clone/equals/toString`。

## 2. Java 用户如何使用
`TextLayout tl = new TextLayout("text", font, frc);` 后 `tl.getAdvance()` 前进宽度、`tl.draw(g2d, x, y)` 绘制、`tl.hitTestChar(x, y).getInsertionIndex()` 定位光标、`tl.getBlackBoxBounds(i, j)` 选区框。是文本排版的完整封装。

## 3. 开源代码实际用法调研
- `java.awt.font.TextLayout` 是 JDK 标准文本排版 API（`src/java.desktop/share/classes/java/awt/font/TextLayout.java`）。
- `test/jdk/java/awt/font/TextLayout/` 系列用例覆盖 advance/ascent/hitTest/caret/justified（本用例范围断言参考，避免硬编码字体像素）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`TextLayoutTest.testBlackBox()`，真实字体 "Hello World"）：
1. characterCount==11；advance/visibleAdvance/ascent>0、descent/leading>=0、bounds 宽>0；LTR、非垂直、ROMAN 基线。
2. getOutline/getPixelBounds 非空；hitTestChar/getCaretShape/getCaretInfo/getNextRightHit 可用。
3. 高亮 shape、black box bounds、justified layout、clone、equals、toString。
白盒（`testWhiteBox()`）：public final、implements Cloneable、关键方法签名。
