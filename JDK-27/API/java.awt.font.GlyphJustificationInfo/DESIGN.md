# java.awt.font.GlyphJustificationInfo 设计案

## 1. API 功能分组
- **常量**：优先级 `PRIORITY_KASHIDA=0/PRIORITY_WHITESPACE=1/PRIORITY_INTERCHAR=2/PRIORITY_NONE=3`。
- **公开字段**（final）：`weight`、`growPriority/growAbsorb/growLeftLimit/growRightLimit`、`shrinkPriority/shrinkAbsorb/shrinkLeftLimit/shrinkRightLimit`。
- **构造**：`GlyphJustificationInfo(weight, growAbsorb, growPriority, growLeftLimit, growRightLimit, shrinkAbsorb, shrinkPriority, shrinkLeftLimit, shrinkRightLimit)`。

## 2. Java 用户如何使用
由 `GlyphVector.getGlyphJustificationInfo(i)` 返回，描述第 i 个 glyph 在两端对齐时可伸缩的权重与优先级。用户读取字段决定排版对齐策略，通常不自行构造。

## 3. 开源代码实际用法调研
- `java.awt.font.TextJustifier`（`src/java.desktop/share/classes/java/awt/font/TextJustifier.java`）使用 GlyphJustificationInfo 的字段计算对齐伸缩量（JDK 内部）。
- `TextLayout.getJustifiedLayout()` 内部经 TextJustifier 消费该信息（JDK 源码）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`GlyphJustificationInfoTest.testBlackBox()`）：
1. 4 个优先级常量精确值。
2. 9 参构造后 9 个 final 字段读回精确值。
3. 独立实例字段一致（无 equals 覆写仍按字段比较）。
白盒（`testWhiteBox()`）：public final class、4 个常量、9 参构造。
