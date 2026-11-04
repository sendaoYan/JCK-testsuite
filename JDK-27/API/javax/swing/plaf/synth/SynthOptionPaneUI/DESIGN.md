# javax.swing.plaf.synth.SynthOptionPaneUI — DESIGN

## 1. API 功能
`SynthOptionPaneUI` 是 Synth LAF 下 `JOptionPane` 的 UI 委托（public 普通类，非抽象，`extends javax.swing.plaf.basic.BasicOptionPaneUI`，实现 `SynthUI`）。静态工厂 `createUI(JComponent)` 直接 `return new SynthOptionPaneUI()`；实例继承 `installUI`/`uninstallUI` 与尺寸方法，`SynthUI` 提供 `getContext(JComponent)`。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthOptionPaneUI.java`（createUI=60）。

## 2. Java 用户如何使用
`SynthOptionPaneUI.createUI(comp)` 得到 UI → `ui.installUI(comp)` 安装 → `((SynthUI) ui).getContext(comp)` 取 `SynthContext`（component/region/style/state）→ 查询 `getPreferredSize` 等尺寸 → `ui.uninstallUI(comp)`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthOptionPaneUI.java:60`：`public static ComponentUI createUI(JComponent c)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthLookAndFeel.java:429`：`createUI` 按 UI class id 反射加载 `SynthOptionPaneUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthContext.java:76`：构造 `SynthContext`。

## 4. 测试用例设计（黑盒部分，`SynthOptionPaneUITest.testBlackBox`）
1. 注入最小 `SynthStyleFactory`（匿名 `SynthStyle` 实现 `getColorForState`/`getFontForState`）。
2. `SynthOptionPaneUI.createUI(comp)` 非空且 `instanceof SynthOptionPaneUI`。
3. `ui.installUI(comp)` 后 `((SynthUI) ui).getContext(comp)` 非空；断言 `getComponent()`、`getRegion().getName()=="OptionPane"`、`getStyle()` 非空。
4. `getPreferredSize`/`getMinimumSize`/`getMaximumSize` 非负。
5. finally `uninstallUI` 并恢复原 style factory。
