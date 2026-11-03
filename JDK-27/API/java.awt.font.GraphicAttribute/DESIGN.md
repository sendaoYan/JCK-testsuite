# java.awt.font.GraphicAttribute 设计案

## 1. API 功能分组
- **常量**：对齐 `TOP_ALIGNMENT=-1/BOTTOM_ALIGNMENT=-2/ROMAN_BASELINE=0/CENTER_BASELINE=1/HANGING_BASELINE=2`。
- **抽象**：`getAscent()/getDescent()/getAdvance()/draw(Graphics2D,float,float)`。
- **模板方法**：`getAlignment()`（final）、`getBounds()`、`getOutline(AffineTransform)`、`getJustificationInfo()`。

## 2. Java 用户如何使用
自定义内嵌图形属性需继承 GraphicAttribute 实现 ascent/descent/advance/draw；两个 JDK 内置实现 `ImageGraphicAttribute`、`ShapeGraphicAttribute` 直接可用。`getJustificationInfo()` 返回两端对齐伸缩信息。

## 3. 开源代码实际用法调研
- `ImageGraphicAttribute`/`ShapeGraphicAttribute` 是 GraphicAttribute 的内置 final 子类（`src/java.desktop/share/classes/java/awt/font/`）。
- `TextLayout`/`TextMeasurer` 对包含 GraphicAttribute 的 AttributedCharacterIterator 排版时调用 `getAscent/getDescent/getAdvance`（JDK 内部）。
- `test/jdk/java/awt/font/TextLayout/` 有用例嵌入 GraphicAttribute 验证内联图形排版。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`GraphicAttributeTest.testBlackBox()`，匿名子类）：
1. 5 个对齐常量精确值。
2. 子类 getAscent/getDescent/getAdvance 读回；getBounds/getOutline 非空。
3. getJustificationInfo 默认无伸缩（weight=0、grow/shrink priority=PRIORITY_NONE）。
白盒（`testWhiteBox()`）：abstract public class、5 个常量、抽象方法与 getAlignment/getBounds 签名。
