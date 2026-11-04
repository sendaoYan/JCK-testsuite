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

---

## 契约深化（本轮追加，`MultiLookAndFeelContractTest`，142 断言）

- **元数据精确**：getName "Multiplexing Look and Feel"、getID "Multiplex"、getDescription "Allows multiple UI instances per component instance"、supported true、native false、无窗口装饰、getDefaults 非 null、toString 含描述。
- **createUIs 收缩契约**（Metal 为默认，MultiLookAndFeel.java:227）：单默认 LaF 下 uis.size()==1 且返回值 == 默认委托本体（非 Multi 包装）；重复调用独立；Label 族收缩到 LabelUI。
- **getUIs**：未收集时返回 **null**（文档化怪癖：空向量→null）；经自身 uis 向量（`ProbeButtonUI extends MultiButtonUI` 暴露 protected 字段）收集 1 个后返回 1 元素数组且恒等。
- **uisToArray**（子类暴露 protected static）：null→长度 0 数组；空→null；两元素保序拷贝。注意 null/空两方向相反，分别断言。
- **30 个 MultiXxxUI 族形**（反射）：具体类、extends 对应基类、public static createUI(JComponent)、getUIs() 返回 ComponentUI[]；MultiButtonUI.createUI(按钮) 收缩到 MetalButtonUI。**无 MultiCheckBoxMenuItemUI/MultiRadioButtonMenuItemUI**（MenuItemUI 一个覆盖全部菜单项）。
- **反例（实测）**：把 MultiLookAndFeel 安装为当前 LaF 会因委托初始化失败抛异常（"Multiplexing LAF: createUI() failed"）——测试只走公共静态 createUIs，不安装 Multi。

### 版本备注
multi 包双侧类型面一致（各 31 类：MultiLookAndFeel + 30 MultiXxxUI）。
