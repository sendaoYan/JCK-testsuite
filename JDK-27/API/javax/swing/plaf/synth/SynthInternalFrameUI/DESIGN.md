# javax.swing.plaf.synth.SynthInternalFrameUI — DESIGN

## 1. API 功能
`SynthInternalFrameUI` 是 Synth LAF 下 `JInternalFrame` 的 UI 委托（public 普通类，非抽象，`extends javax.swing.plaf.basic.BasicInternalFrameUI`，实现 `SynthUI`）。静态工厂 `createUI(JComponent)` 直接 `return new SynthInternalFrameUI()`；实例继承 `installUI`/`uninstallUI` 与尺寸方法，`SynthUI` 提供 `getContext(JComponent)`。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthInternalFrameUI.java`（createUI=55）。

## 2. Java 用户如何使用
`SynthInternalFrameUI.createUI(comp)` 得到 UI → `ui.installUI(comp)` 安装 → `((SynthUI) ui).getContext(comp)` 取 `SynthContext`（component/region/style/state）→ 查询 `getPreferredSize` 等尺寸 → `ui.uninstallUI(comp)`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthInternalFrameUI.java:55`：`public static ComponentUI createUI(JComponent c)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthLookAndFeel.java:429`：`createUI` 按 UI class id 反射加载 `SynthInternalFrameUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthContext.java:76`：构造 `SynthContext`。

## 4. 测试用例设计（黑盒部分，`SynthInternalFrameUITest.testBlackBox`）
1. `createUI(comp)` 非空且 `instanceof SynthInternalFrameUI`。
2. `((SynthUI) ui).getContext(comp)` 非空，断言 `getComponent()` 与 `getRegion().getName()=="InternalFrame"`。
（降级：不调用 `installUI`——`SynthInternalFrameUI` 安装会在 Basic 层触发对空 chooser 的 uninstall 或递归，故仅覆盖 createUI + getContext。）
