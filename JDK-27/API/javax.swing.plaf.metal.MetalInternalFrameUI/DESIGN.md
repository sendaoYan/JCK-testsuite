# javax.swing.plaf.metal.MetalInternalFrameUI — DESIGN

## 1. API 功能
`MetalInternalFrameUI` 是 Metal 观感下 `JInternalFrame` 的 UI 委托（`extends BasicInternalFrameUI`，非抽象）。方法分组：工厂 `createUI(JComponent)`、生命周期 `installUI/uninstallUI`、`setPalette(boolean)` 调色板模式、以及尺寸查询与 `paint`。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JInternalFrame` 自动挂载本委托。直接使用时：`JInternalFrame f = new JInternalFrame("t"); MetalInternalFrameUI ui = new MetalInternalFrameUI(f); ui.installUI(f); ui.setPalette(true); ui.paint(g, f); ui.uninstallUI(f);`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalInternalFrameUI.java:43`：`public class MetalInternalFrameUI extends BasicInternalFrameUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:292`：将 `"InternalFrameUI"` 映射到 `MetalInternalFrameUI`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/plaf/metal/MetalInternalFrameUI/Test6657026.java:40`：`public class Test6657026 extends MetalInternalFrameUI implements Runnable` 验证共享委托。

## 4. 测试用例设计（黑盒部分，`MetalInternalFrameUITest.testBlackBox`）
1. `createUI` 返回本类；`installUI(f)` 无异常。
2. `setPalette(true)` 无异常；`getMaximumSize` 返回非空（固定 `Integer.MAX_VALUE` 尺寸契约）。
3. `paint` 画到 `BufferedImage` 无异常；`uninstallUI(f)` 无异常。
