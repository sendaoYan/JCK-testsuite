# java.awt.FontMetrics 黑盒行为测试设计

## 1. API 功能
- 抽象类，经 `Graphics.getFontMetrics(Font)` 获取具体实例。
- 查询：`getFont`、`getFontRenderContext`、`getLeading/getAscent/getDescent/getHeight`、`getMaxAscent/getMaxDescent/getMaxDecent/getMaxAdvance`、`getWidths`、`hasUniformLineMetrics`。
- 度量：`charWidth(int/char)`、`stringWidth(String)`、`charsWidth(char[],off,len)`、`bytesWidth(byte[],off,len)`。
- 高级：`getLineMetrics(...)`、`getStringBounds(...)`、`getMaxCharBounds(Graphics)`。
- `toString`。

## 2. Java 用户如何使用
字符串排版/命中定位：`stringWidth` 计算文本宽度，`getAscent/getDescent` 定位基线，`getHeight` 计算行高。
```java
FontMetrics fm = g.getFontMetrics(font);
int w = fm.stringWidth(text);
int lineHeight = fm.getHeight();
int y = baseline + fm.getAscent();
```
组合链路：`createGraphics` → `getFontMetrics` → `getFont` 校验 → `stringWidth` 关系 → 度量关系。

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/FontMetrics.java`：`getHeight()`=`getLeading()+getAscent()+getDescent()`；`charWidth(char)`（<256）取 `getWidths()[ch]`；`stringWidth` 走 `charsWidth`；`getWidths()` 固定 256 长度。
- JDK 自带测试（真实用法）：`test/jdk/java/awt/Component/Headless/HeadlessPanel.java`（经 `getFontMetrics` 度量）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- `getFont` 返回传入字体；`getFontRenderContext` 非空。
- 度量关系：非负；`height==leading+ascent+descent`；`height>=ascent+descent`；`maxAscent>=ascent`；`maxDescent>=descent`；`maxAdvance>0`。
- 宽度关系（不精确断像素）：`stringWidth("MMM")>"M"`、`"MMM">"I"`、size24>size12；`charWidth('M')==stringWidth("M")`；`charWidth(char)==charWidth(int)`。
- `charsWidth` 等于 `stringWidth`；`bytesWidth`（latin-1）等于 `stringWidth`。
- `getWidths` 长度 256 且非负。
- `getLineMetrics`/`getStringBounds`/`getMaxCharBounds` 关系与非负/正宽。
- `toString` 含 ascent/height。
断言 51 项，只断关系不断精确像素（headless-safe）。
