# java.awt.font.LineMetrics 设计案

## 1. API 功能分组
- **抽象类**：`LineMetrics`，由 `Font.getLineMetrics(...)` 返回。
- **度量**：`getNumChars/getAscent/getDescent/getLeading/getHeight`。
- **基线**：`getBaselineIndex()`、`getBaselineOffsets()`。
- **装饰线**：`getUnderlineOffset/getUnderlineThickness/getStrikethroughOffset/getStrikethroughThickness`。

## 2. Java 用户如何使用
`LineMetrics lm = font.getLineMetrics("text", frc);` 取整行度量：`lm.getHeight()` 行高、`lm.getAscent()/getDescent()` 上下延伸、`lm.getUnderlineOffset()/getUnderlineThickness()` 画下划线。用于自绘文本行的垂直定位。

## 3. 开源代码实际用法调研
- `Font.getLineMetrics` 是标准入口（`src/java.desktop/share/classes/java/awt/Font.java`）。
- `java.awt.font.TextLayout` 用 LineMetrics 计算行高与基线偏移（JDK 内部）。
- `test/jdk/java/awt/font/LineBreakMeasurer/`、`FontMetrics` 相关用例断言 ascent/descent/height 关系（本用例范围断言参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`LineMetricsTest.testBlackBox()`，真实字体 "Hello"）：
1. numChars==5；ascent>0、descent>=0、leading>=0、height>=ascent+descent。
2. baselineIndex==ROMAN；baselineOffsets 长度>=3。
3. underline offset 为负（基线下方）、thickness>0；strikethrough offset/thickness>0。
白盒（`testWhiteBox()`）：abstract public class、度量方法签名。
