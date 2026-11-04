# java.awt.font.TextMeasurer 设计案

## 1. API 功能分组
- **构造**：`TextMeasurer(AttributedCharacterIterator, FontRenderContext)`。
- **度量**：`getLineBreakIndex(int start, float width)`、`getAdvanceBetween(int start, int limit)`、`getLayout(int start, int limit)`。
- **编辑**：`insertChar(AttributedCharacterIterator, int)`、`deleteChar(AttributedCharacterIterator, int)`。
- **接口**：implements Cloneable。

## 2. Java 用户如何使用
低层文本度量：`TextMeasurer tm = new TextMeasurer(text.getIterator(), frc);` 后 `tm.getAdvanceBetween(0, n)` 取前 n 字符宽度、`tm.getLayout(0, n)` 取排版、`tm.getLineBreakIndex(0, width)` 找断行点。LineBreakMeasurer 内部封装了 TextMeasurer。

## 3. 开源代码实际用法调研
- `java.awt.font.LineBreakMeasurer` 内部持有 TextMeasurer 并委托度量（`src/java.desktop/share/classes/java/awt/font/LineBreakMeasurer.java`）。
- `TextLayout` 也经 TextMeasurer 计算（JDK 内部）。
- `test/jdk/java/awt/font/TextMeasurer/` 用例验证 getAdvanceBetween/getLineBreakIndex 与 getLayout（本用例范式参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`TextMeasurerTest.testBlackBox()`）：
1. getLineBreakIndex(0,1000) 在文本内；getAdvanceBetween(0,3)>0；getLayout(0,3) 字符数 3。
2. insertChar/deleteChar 后度量器仍可用。
白盒（`testWhiteBox()`）：public final、implements Cloneable、度量与编辑方法签名。
