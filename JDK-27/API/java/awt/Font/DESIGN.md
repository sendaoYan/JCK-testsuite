# java.awt.Font 黑盒行为测试设计

## 1. API 功能
- 常量：逻辑字体名 `DIALOG/DIALOG_INPUT/SANS_SERIF/SERIF/MONOSPACED`；样式 `PLAIN/BOLD/ITALIC`；基线 `ROMAN/CENTER/HANGING_BASELINE`；字体格式 `TRUETYPE_FONT/TYPE1_FONT`；布局标志 `LAYOUT_*`。
- 构造：`(String,int,int)`（逻辑/物理名+样式+尺寸）、`(Map<Attribute,?>)`。
- 查询：`getFamily`、`getName`、`getFontName`、`getPSName`、`getStyle`、`getSize`、`getSize2D`、`isPlain/isBold/isItalic`、`isTransformed`、`getTransform`、`getAttributes`、`getAvailableAttributes`、`getNumGlyphs`、`getMissingGlyphCode`、`getBaselineFor`、`canDisplay`、`canDisplayUpTo`、`hasUniformLineMetrics`。
- 变换：`deriveFont`（style/size/transform/attributes 六种重载）。
- 静态工具：`decode`、`getFont`、`createFont(s)`、`textRequiresLayout`。
- 度量/字形：`getLineMetrics`、`getStringBounds`、`getMaxCharBounds`、`createGlyphVector`、`layoutGlyphVector`。

## 2. Java 用户如何使用
逻辑字体名构造字体对象，组件 `setFont`，度量用 `FontMetrics`/`FontRenderContext`，派生用 `deriveFont`。
```java
Font f = new Font(Font.SANS_SERIF, Font.BOLD, 24);
Font small = f.deriveFont(12f);
Font italic = f.deriveFont(Font.ITALIC);
Font fromStr = Font.decode("Dialog-PLAIN-14");
if (f.canDisplay('中')) { ... }
```
组合链路：`decode`/构造 → `deriveFont` → `getStyle/getSize` → `equals` 比较 → 度量关系断言。

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/Font.java`：逻辑字体 `getFamily()` 返回逻辑名（Dialog/SansSerif/Serif/Monospaced/DialogInput）；`decode(null)` 返回 `Dialog, PLAIN, 12`；`decode` 用 `-`/空格分隔 style 与 size；`toString`=`java.awt.Font[family=..,name=..,style=..,size=..]`；`deriveFont` 按 style/size 构造新实例；`equals` 含 transform 语义。
- JDK 自带测试（真实用法）：`test/jdk/java/awt/Component/Headless/HeadlessPanel.java`（`new Font` + 组件 `setFont`）、`test/jdk/java/awt/Graphics2D/DrawString/EmptyAttrString.java`（`new Font` + `canDisplay`）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 常量数值精确断言（样式/基线/字体格式/布局标志，已按 JDK-27 实测值校正）。
- 构造 + `getFamily/getName/getStyle/getSize/getSize2D/is*`。
- `deriveFont` 六种重载 + `equals` 契约（派生字体等于直接构造）。
- `decode`（含 `-BOLD-24`、纯尺寸、`null`→Dialog/PLAIN/12、`-italic-18`）。
- `canDisplay`/`canDisplayUpTo`（ASCII 恒 `-1`）。
- 字形/基线：`getNumGlyphs>0`、`getMissingGlyphCode>=0`、`getBaselineFor('A')==ROMAN`。
- 属性：`getAttributes` 含 FAMILY/SIZE；`getAvailableAttributes` 含 FAMILY。
- 度量关系（不精确断像素）：`getLineMetrics` 非负 + height>=ascent+descent；`getStringBounds("MMM")` size24>size12、长串>短串；`getMaxCharBounds` 宽>0；`createGlyphVector` 字形数。
- 组合链：`decode`→`deriveFont(size)`→`deriveFont(style)`→等于直接构造。
断言 106 项，字体像素只断关系不断精确值（headless-safe）。
