# javax.swing.plaf.synth.SynthEditorPaneUI — DESIGN

## 1. API 功能
`SynthEditorPaneUI` 是 Synth LAF 下 `JEditorPane` 的 UI 委托（public 普通类，非抽象，`extends javax.swing.plaf.basic.BasicEditorPaneUI`，实现 `SynthUI`）。静态工厂 `createUI(JComponent)` 直接 `return new SynthEditorPaneUI()`；实例继承 `installUI`/`uninstallUI` 与尺寸方法，`SynthUI` 提供 `getContext(JComponent)`。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthEditorPaneUI.java`（createUI=64）。

## 2. Java 用户如何使用
`SynthEditorPaneUI.createUI(comp)` 得到 UI → `ui.installUI(comp)` 安装 → `((SynthUI) ui).getContext(comp)` 取 `SynthContext`（component/region/style/state）→ 查询 `getPreferredSize` 等尺寸 → `ui.uninstallUI(comp)`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthEditorPaneUI.java:64`：`public static ComponentUI createUI(JComponent c)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthLookAndFeel.java:429`：`createUI` 按 UI class id 反射加载 `SynthEditorPaneUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthContext.java:76`：构造 `SynthContext`。

## 4. 测试用例设计（黑盒部分，`SynthEditorPaneUITest.testBlackBox`）
1. 注入最小 `SynthStyleFactory`（匿名 `SynthStyle` 实现 `getColorForState`/`getFontForState`）。
2. `SynthEditorPaneUI.createUI(comp)` 非空且 `instanceof SynthEditorPaneUI`。
3. `ui.installUI(comp)` 后 `((SynthUI) ui).getContext(comp)` 非空；断言 `getComponent()`、`getRegion().getName()=="EditorPane"`、`getStyle()` 非空。
4. `getPreferredSize`/`getMinimumSize`/`getMaximumSize` 非负。
5. finally `uninstallUI` 并恢复原 style factory。
