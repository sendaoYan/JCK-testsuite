# java.awt.font.GlyphMetrics 设计案

## 1. API 功能分组
- **常量**（byte）：glyph 类型 `STANDARD=0/LIGATURE=1/COMBINING=2/COMPONENT=3/WHITESPACE=4`。
- **构造**：`GlyphMetrics(advance, bounds, type)`、`GlyphMetrics(horizontal, advanceX, advanceY, bounds, type)`。
- **查询**：`getAdvance/getAdvanceX/getAdvanceY/getBounds2D/getType`；`isStandard/isLigature/isCombining/isComponent/isWhitespace`。

## 2. Java 用户如何使用
从 `GlyphVector.getGlyphMetrics(i)` 拿第 i 个 glyph 的度量：`gm.getAdvance()` 前进宽度、`gm.getBounds2D()` 视觉边界、`gm.isWhitespace()` 判空格。用于自定义文本布局与光标定位。

## 3. 开源代码实际用法调研
- `java.awt.font.GlyphVector.getGlyphMetrics(int)` 返回 GlyphMetrics（JDK 源码，标准访问点）。
- `TextLayout`/`TextLine` 内部用 GlyphMetrics 计算 advance 与边界（`src/java.desktop/share/classes/java/awt/font/TextLine.java`）。
- `test/jdk/java/awt/font/GlyphVector/` 用例用 getGlyphMetrics 断言 advance 与 bounds（本用例度量范式一致）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`GlyphMetricsTest.testBlackBox()`）：
1. 5 个类型常量精确值。
2. 真实字体 "H" 的 advance>0、advanceX>0/advanceY==0、bounds 非空、type STANDARD。
3. 空格 glyph 为 WHITESPACE。
4. 手动构造器字段 round-trip。
白盒（`testWhiteBox()`）：public final class、5 个 byte 常量、查询方法签名。
