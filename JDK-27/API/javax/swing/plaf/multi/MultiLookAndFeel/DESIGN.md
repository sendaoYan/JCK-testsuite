# javax.swing.plaf.multi.MultiLookAndFeel — DESIGN

## 1. API 功能
`MultiLookAndFeel` 是 Swing 复用（multiplexing）LookAndFeel，继承 `javax.swing.LookAndFeel`。核心成员：
- 构造/工厂：`public MultiLookAndFeel()`；`public static ComponentUI createUIs(ComponentUI, Vector<ComponentUI>, JComponent)`；`protected static ComponentUI[] uisToArray(Vector<? extends ComponentUI>)`。
- L&F 元信息：`getName()`、`getID()`、`getDescription()`、`isNativeLookAndFeel()`、`isSupportedLookAndFeel()`。
- 默认表：`getDefaults()` 返回把 UI 类 ID（如 "ButtonUI"）映射到 `javax.swing.plaf.multi.Multi*UI` 的 `UIDefaults`。

## 2. Java 用户如何使用
- 用 `UIManager.addAuxiliaryLookAndFeel(...)` 注册辅助 L&F，Swing 在 `createUI` 时通过复用 L&F 组合多个 UI（见 `UIManager.getMultiLookAndFeel`）。
- 直接黑盒用法：`MultiLookAndFeel laf = new MultiLookAndFeel();` 后读 `laf.getName()/getID()/getDescription()/isNativeLookAndFeel()/isSupportedLookAndFeel()/getDefaults()`；`MultiLookAndFeel.createUIs(mui, mui.uis, component)` 把默认+辅助 L&F 的 UI 填入向量。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.desktop/share/classes/javax/swing/UIManager.java`（约 1085–1101、1120–1140）：`getMultiLookAndFeel()` 装载 `javax.swing.plaf.multi.MultiLookAndFeel`；`addAuxiliaryLookAndFeel`/`getAuxiliaryLookAndFeels` 管理辅助 L&F；`getUI(target)` 在复用模式下调 `multiLAF.getDefaults().getUI(target)`。
- `jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/multi/MultiLookAndFeel.java`：`getDefaults()` 内联 `uiDefaults` 数组，把 "ButtonUI" 等映射到 `javax.swing.plaf.multi.Multi*UI`。
- `jdk25u-dev/test/jdk/javax/swing/LookAndFeel/isMultiLAFLockedCorrectly.java`：反射实例化 `MultiLookAndFeel` 并断言 `isSupportedLookAndFeel()==true`。

## 4. 测试用例设计（黑盒部分，`MultiLookAndFeelTest.testBlackBox`）
1. `new MultiLookAndFeel()` 后 `getName()=="Multiplexing Look and Feel"`、`getID()=="Multiplex"`、`getDescription()` 为固定描述串。
2. `isNativeLookAndFeel()==false`、`isSupportedLookAndFeel()==true`。
3. `getDefaults()` 非 null，且 "ButtonUI"→`...MultiButtonUI`、"LabelUI"→`...MultiLabelUI`、"TextFieldUI"→`...MultiTextUI` 映射正确。
