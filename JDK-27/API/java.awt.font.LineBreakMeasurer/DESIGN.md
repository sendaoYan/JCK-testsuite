# java.awt.font.LineBreakMeasurer 设计案

## 1. API 功能分组
- **构造**：`LineBreakMeasurer(AttributedCharacterIterator, FontRenderContext)`、`LineBreakMeasurer(AttributedCharacterIterator, BreakIterator, FontRenderContext)`。
- **分行**：`nextOffset(float wrappingWidth)`、`nextOffset(float, int limit, boolean requireNextWord)`、`nextLayout(float)`、`nextLayout(float, int, boolean)`。
- **位置**：`getPosition()`、`setPosition(int)`。
- **编辑**：`insertChar(AttributedCharacterIterator, int)`、`deleteChar(AttributedCharacterIterator, int)`。

## 2. Java 用户如何使用
逐行断行：`LineBreakMeasurer lbm = new LineBreakMeasurer(text.getIterator(), frc); while (lbm.getPosition() < end) { TextLayout line = lbm.nextLayout(width); ... }`，在固定行宽内切分段落并绘制。

## 3. 开源代码实际用法调研
- `java.awt.font.LineBreakMeasurer` 内部委托 `TextMeasurer` 做度量与断行（`src/java.desktop/share/classes/java/awt/font/LineBreakMeasurer.java`）。
- `test/jdk/java/awt/font/LineBreakMeasurer/` 系列用例验证 nextOffset/nextLayout 的断行位置与文本行排版（本用例 nextOffset/nextLayout 范式一致）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`LineBreakMeasurerTest.testBlackBox()`）：
1. 初始 position==0；nextOffset(1000) 在文本内、nextLayout 返回 advance>0 的布局、position 到文本末尾。
2. setPosition 重置；显式 BreakIterator 构造。
3. insertChar/deleteChar 推进/回退 position。
白盒（`testWhiteBox()`）：public final class、nextOffset/nextLayout/getPosition/setPosition/insertChar/deleteChar 签名。
