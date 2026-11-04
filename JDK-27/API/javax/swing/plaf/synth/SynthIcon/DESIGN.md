# javax.swing.plaf.synth.SynthIcon — DESIGN

## 1. API 功能
`SynthIcon extends Icon`（public interface），为 Synth 提供带 `SynthContext` 的图标协议：`paintIcon(SynthContext, Graphics, int, int, int, int)`、`getIconWidth(SynthContext)`、`getIconHeight(SynthContext)`；继承的 `Icon` 默认方法 `paintIcon(Component, Graphics, int, int)`/`getIconWidth()`/`getIconHeight()` 委托给 context 变体。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthIcon.java:37`。

## 2. Java 用户如何使用
匿名实现三个抽象方法，由 Synth 组件以 `SynthContext` 调用：`icon.getIconWidth(ctx)`；也可作为普通 `Icon` 使用：`icon.paintIcon(comp, g, x, y)`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthIcon.java:37`：`public interface SynthIcon extends Icon`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthStyle.java:1027`：`getIcon(SynthContext, Object)` 返回 `Icon`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthGraphicsUtils.java:445/463`：`getIconWidth/getIconHeight(Icon, SynthContext)` 对 `SynthIcon` 走 context 变体。

## 4. 测试用例设计（黑盒部分，`SynthIconTest.testBlackBox`）
1. 匿名实现 `instanceof Icon`。
2. `getIconWidth()`/`getIconHeight()` 默认委托到 `getIconWidth(null)`/`getIconHeight(null)`。
3. `getIconWidth(null)`/`getIconHeight(null)` 直接走实现。
4. `paintIcon((Component)null, (Graphics)null, 3, 4)` 默认委托到 context 变体并置 painted 标记。
