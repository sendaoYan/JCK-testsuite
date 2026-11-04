# javax.swing.plaf.metal.MetalProgressBarUI — DESIGN

## 1. API 功能
`MetalProgressBarUI` 是 Metal 观感下 `JProgressBar` 的 UI 委托（`extends BasicProgressBarUI`，非抽象）。方法分组：工厂 `createUI(JComponent)`、生命周期 `installUI/uninstallUI`，以及公开重写的 `paintDeterminate(Graphics, JComponent)` 与 `paintIndeterminate(Graphics, JComponent)`。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JProgressBar` 自动挂载本委托。直接使用时：`MetalProgressBarUI ui = new MetalProgressBarUI(); ui.installUI(p); p.setIndeterminate(true); ui.paintIndeterminate(g, p); ui.uninstallUI(p);`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalProgressBarUI.java:47`：`public class MetalProgressBarUI extends BasicProgressBarUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalProgressBarUI.java:73` 与 `:153`：公开重写 `paintDeterminate` 与 `paintIndeterminate`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:296`：将 `"ProgressBarUI"` 映射到 `MetalProgressBarUI`。

## 4. 测试用例设计（黑盒部分，`MetalProgressBarUITest.testBlackBox`）
1. `createUI` 返回本类；`installUI(p)` 无异常。
2. 设置大小后 `paintDeterminate` 画到 `BufferedImage` 无异常。
3. `setIndeterminate(true)` 后 `paintIndeterminate` 无异常（进入不确定状态以初始化内部矩形）。
4. `uninstallUI` 无异常。
