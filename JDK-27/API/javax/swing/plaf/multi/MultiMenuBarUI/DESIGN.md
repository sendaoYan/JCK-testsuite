# javax.swing.plaf.multi.MultiMenuBarUI — DESIGN

## 1. API 功能
MultiMenuBarUI 是 Swing 复用（multiplexing）UI 委托，继承 `javax.swing.plaf.MenuBarUI`，用于把默认 L&F 与若干辅助 L&F 的 UI 组合到同一组件上。核心成员：
- 字段：`protected Vector<ComponentUI> uis`（真实 UI 列表；第一项是默认 L&F 的 UI）。
- 构造/工厂：`public MultiMenuBarUI()`；`public static ComponentUI createUI(JComponent)`（内部调用 `MultiLookAndFeel.createUIs(mui, mui.uis, target)`）。
- 查询：`ComponentUI[] getUIs()` 返回 UI 数组；`boolean contains(JComponent,int,int)`。
- 生命周期/绘制：`installUI/uninstallUI/update/paint` 以及 `getPreferredSize/getMinimumSize/getMaximumSize/getAccessibleChildrenCount/getAccessibleChild`。

## 2. Java 用户如何使用
- 通常不直接 `new`，而是注册复用 L&F（`UIManager.addAuxiliaryLookAndFeel(...)`）后由 Swing 在 `createUI` 时自动组合；默认路径 `UIManager.getDefaults().getUI(component)` 会把 UI 类 ID（如 "MenuBarUI"）解析为 `javax.swing.plaf.multi.MultiMenuBarUI`。
- 直接黑盒用法：`ComponentUI ui = MultiMenuBarUI.createUI(component);` 得到默认（无辅助 L&F 时为单 UI）；`MultiMenuBarUI m = new MultiMenuBarUI(); MultiLookAndFeel.createUIs(m, m.uis, component);` 填充 `uis` 后再 `m.installUI(component)`、`m.getUIs()`、`m.getPreferredSize(component)`、`m.paint(g, component)`、`m.contains(component,x,y)`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/multi/MultiLookAndFeel.java` `getDefaults()` 把 UI 类 ID "MenuBarUI" 映射为 "javax.swing.plaf.multi.MultiMenuBarUI"（复用 L&F 的默认表）。
- `jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/multi/MultiMenuBarUI.java`：AutoMulti 自动生成，`createUI` 调 `MultiLookAndFeel.createUIs(mui, mui.uis, a)`；各方法把调用分发给 `uis` 中每个 UI，返回值取 `uis.elementAt(0)`。
- `jdk25u-dev/src/java.desktop/share/classes/javax/swing/UIManager.java`（约 1085–1101、1120–1140）：`getMultiLookAndFeel()` 装载复用 L&F，`addAuxiliaryLookAndFeel`/`getAuxiliaryLookAndFeels` 管理辅助 L&F。
- `jdk25u-dev/test/jdk/javax/swing/LookAndFeel/isMultiLAFLockedCorrectly.java`：反射实例化 `MultiLookAndFeel` 并断言 `isSupportedLookAndFeel()==true`（其余 Multi*UI 无独立 test/jdk 用例，仅经 getDefaults 映射被引用）。

## 4. 测试用例设计（黑盒部分，`MultiMenuBarUITest.testBlackBox`）
1. `MultiMenuBarUI.createUI(comp)` 返回非 null 的 `ComponentUI`（无辅助 L&F 时为默认单 UI）。
2. `MultiLookAndFeel.createUIs(...)` 用本地向量填充默认 UI，数量 >= 1；`new MultiMenuBarUI()` 初始 `uis` 为空。
3. 用组件自带的已安装 UI 填充 `uis`（`Exposed.seedFrom`），`getUIs()` 返回长度 1 且首元素等于组件 UI。
4. `paint/update` 写入 `BufferedImage` 图形上下文无异常；`contains`/`getAccessibleChildrenCount`/尺寸方法与底层 UI 分发一致；`getAccessibleChild(comp,0)` 无异常。
5. 各专属方法按组件类型组合断言（见黑盒代码），最后 `installUI/uninstallUI` 无异常。
