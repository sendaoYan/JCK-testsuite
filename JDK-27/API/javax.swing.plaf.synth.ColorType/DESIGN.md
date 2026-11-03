# javax.swing.plaf.synth.ColorType — DESIGN

## 1. API 功能
`ColorType` 是 Synth 颜色查询的键类型（public 普通类，非抽象，`extends Object`，无接口）。常量 `FOREGROUND`/`BACKGROUND`/`TEXT_FOREGROUND`/`TEXT_BACKGROUND`/`FOCUS` 与 `MAX_COUNT`；方法 `getID()` 与 `toString()`（返回描述串）。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/ColorType.java`（FOREGROUND=67、BACKGROUND=72、TEXT_FOREGROUND=77、TEXT_BACKGROUND=83、FOCUS=89、MAX_COUNT=94）。

## 2. Java 用户如何使用
作为 `SynthStyle.getColor(SynthContext, ColorType)` 的第二个参数传入：`style.getColor(ctx, ColorType.FOREGROUND)`；`ColorType.MAX_COUNT` 用于自定义子类分配 id。自定义子类：`new ColorType("Custom") {}`（protected 构造器），`toString()` 返回构造描述。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthStyle.java:760`：`public Color getColor(SynthContext context, ColorType type)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthStyle.java` 内部各 `get*ForState` 实现按 `type.getID()` 分派。
- `test/jdk/javax/swing/plaf/synth/` 多个 UI 测试以 `ColorType.BACKGROUND` 断言合成背景色。

## 4. 测试用例设计（黑盒部分，`ColorTypeTest.testBlackBox`）
1. 5 个预置常量非空，`MAX_COUNT == 5`。
2. 每个常量 `getID()` 精确等于 0..4。
3. 每个常量 `toString()` 返回 "Foreground"/"Background"/"TextForeground"/"TextBackground"/"Focus"。
4. 常量两两不同。
5. 自定义子类 `new ColorType("Custom") {}`：`getID()==MAX_COUNT`，`toString()=="Custom"`。
6. `new ColorType(null) {}` 抛 `NullPointerException`。
