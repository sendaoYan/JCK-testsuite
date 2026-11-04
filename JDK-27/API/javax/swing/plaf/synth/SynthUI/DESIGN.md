# javax.swing.plaf.synth.SynthUI — DESIGN

## 1. API 功能
public interface，`extends SynthConstants`，不可实例化。声明两个抽象方法：
- `public SynthContext getContext(JComponent c)`。
- `public void paintBorder(SynthContext, Graphics, int, int, int, int)`。

## 2. Java 用户如何使用
由具体 Synth*UI 实现（如 SynthButtonUI、SynthTreeUI、SynthViewportUI 等）。用户通过 `ui.getContext(comp)` 取得上下文、`ui.paintBorder(ctx, g, x, y, w, h)` 绘制边框。

## 3. 开源用法调研（来源：jdk25u-dev 源码 + test/jdk）
1. `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthLookAndFeel.java:287-352`：`SynthStyle.updateStyle(SynthContext, SynthUI)` 消费 SynthUI 实例。
2. `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthContext.java`：`SynthContext` 作为 getContext 的返回类型。
3. 具体实现：`SynthButtonUI.java`、`SynthTreeUI.java`、`SynthViewportUI.java` 均 `implements SynthUI`。

## 4. 测试用例设计（黑盒部分，`SynthUITest.testBlackBox`）
纯反射：断言 SynthUI 是接口、`getContext`/`paintBorder` 为 abstract、`getContext` 返回 SynthContext；并断言 SynthButtonUI/SynthTreeUI/SynthViewportUI 均 `isAssignableFrom` SynthUI。
