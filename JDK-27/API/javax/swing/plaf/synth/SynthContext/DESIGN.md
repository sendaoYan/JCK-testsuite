# javax.swing.plaf.synth.SynthContext — DESIGN

## 1. API 功能
`SynthContext` 是 Synth 样式查询的上下文（public 普通类，非抽象，`extends Object`，无接口）。构造器 `(JComponent, Region, SynthStyle, int state)`；getter `getComponent()`/`getRegion()`/`getStyle()`/`getComponentState()`。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthContext.java`（class=40、构造器=76、getRegion=104、getStyle=124、getComponentState=142）。

## 2. Java 用户如何使用
由 UI 在 `getContext(JComponent)` 里创建并返回，样式方法统一签名 `getColor(SynthContext, ColorType)` 使用；用户也可自行 `new SynthContext(comp, Region.LABEL, style, SynthConstants.ENABLED)`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthContext.java:76`：构造器保存四个字段并判空。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthStyle.java:760`：`getColor(SynthContext, ColorType)` 以 context 取 state。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthLookAndFeel.java:341`：`getRegion(JComponent)` 供构造 region。

## 4. 测试用例设计（黑盒部分，`SynthContextTest.testBlackBox`）
1. 构造后四个 getter 精确返回传入值。
2. `null` component/region/style 分别抛 `NullPointerException`。
