# javax.swing.plaf.synth.SynthTableUI — DESIGN

## 1. API 功能
- 工厂：`public static ComponentUI createUI(JComponent c)` 返回 `new SynthTableUI()`。
- 查询：`public SynthContext getContext(JComponent c)` 返回组件状态上下文（`SynthContext` 含 component/region/style/state）。
- 绘制：`public void paintBorder(SynthContext, Graphics, int, int, int, int)`（SynthUI 接口约定），`public void update/paint(Graphics, JComponent)`。
- 尺寸：`getPreferredSize/getMinimumSize/getMaximumSize(JComponent)`（部分继承 Basic*UI / ComponentUI）。
- 生命周期：`installUI/uninstallUI/installDefaults/uninstallDefaults/installListeners/uninstallListeners`（继承或覆写）。
- 事件：`propertyChange(PropertyChangeEvent)`（多数实现 PropertyChangeListener）。

## 2. Java 用户如何使用
典型链路（安装 + 查询 + 绘制 + 卸载）：
1. `JComponent c = new JXxx();`（对应组件）
2. `ComponentUI ui = SynthTableUI.createUI(c);`（或直接 `new SynthTableUI()`）
3. `ui.installUI(c);` —— 需要先通过 `SynthLookAndFeel` 安装样式工厂，否则 `updateStyle` 中 `SynthLookAndFeel.getStyleFactory()` 为 null 会抛 NPE。
4. `SynthContext ctx = ui.getContext(c);`、`ui.getPreferredSize(c)`、`ui.paintBorder(ctx, g, 0, 0, w, h)`。
5. `ui.uninstallUI(c);`。

说明：尺寸/绘制多数依赖 installUI 期间写入的内部组件字段与 SynthStyle，故在“未安装 + 未启用 SynthLookAndFeel”环境下确定性返回 null 或抛 NPE（详见第 4 节）。

## 3. 开源用法调研（来源：jdk25u-dev 源码 + test/jdk）
1. `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthTableUI.java:107`：`public static ComponentUI createUI(JComponent) { return new SynthTableUI(); }`，标准工厂方法。
2. `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthLookAndFeel.java:643`：`Region.registerUIs(table)`，把 UI 类注册到 Region 映射；`SynthLookAndFeel.java:490-559` 的 `createUI` 分发据此返回对应 Synth*UI。
3. `test/jdk/javax/swing/plaf/synth/6771547/SynthTest.java:39-46`：加载 Synth 样式、`UIManager.setLookAndFeel(laf)`、再 `new JButton()` 校验颜色——Synth 全组件 UI 的通用接入方式。
4. `test/jdk/javax/swing/plaf/synth/SynthToolBarUI/8136363/ComponentSetNameTest.java:59-71`：对一组 Swing 组件（含本类对应组件）逐个 setLookAndFeel 后校验背景色。

## 4. 测试用例设计（黑盒部分，`SynthTableUITest.testBlackBox`）
- `createUI(comp)`：非空、精确类型 `SynthTableUI`、`instanceof SynthUI`。
- `getContext(comp)`：非空且 `getComponent()==comp`、`getRegion()` 非空（少数类如菜单项在未安装时确定性抛 NPE，用 expectNPE 断言）。
- `paintBorder(new SynthContext(...), offscreen BufferedImage 的 Graphics)`：验证 SynthUI 契约在显式传入 style 时可离屏完成（对依赖内部组件字段的 4 个类断言确定性 NPE）。
- 尺寸方法：三个尺寸方法依赖 table 字段（NPE），分别用“非空且宽高≥0”“==null”“expectNPE”三种方式确定性地断言。
- `installUI(comp)`：未启用 SynthLookAndFeel 时确定性抛 NPE（expectNPE），用于记录 LAF 前置依赖。
