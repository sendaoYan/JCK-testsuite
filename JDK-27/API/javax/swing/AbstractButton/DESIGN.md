# javax.swing.AbstractButton 设计案

## 1. API 功能
`AbstractButton` 是所有 Swing 按钮（JButton/JToggleButton/JCheckBox/JRadioButton/JMenuItem）的抽象基类，继承 `JComponent`，实现 `ItemSelectable` 与 `SwingConstants`。方法分组：
- 文本/图标：`getText/setText`、`setIcon/getIcon` 等图标族、`setIconTextGap`。
- 选择状态：`isSelected/setSelected`、`doClick()`、`doClick(int)`。
- 模型：`getModel/setModel`（`ButtonModel`）、`setActionCommand/getActionCommand`。
- 外观：`setBorderPainted/isBorderPainted`、`setContentAreaFilled`、`setRolloverEnabled`、`setMargin/getMargin`、`setMnemonic/getMnemonic`。
- 监听：`addActionListener/getActionListeners`、`addItemListener`、`addChangeListener`。

## 2. Java 用户如何使用
用户不直接 new `AbstractButton`（抽象类），而是用具体子类；或匿名继承它并安装模型：
```java
AbstractButton b = new AbstractButton() {};
b.setModel(new DefaultButtonModel());
b.setText("Click"); b.setMnemonic('C');
b.addActionListener(e -> System.out.println("clicked"));
b.doClick();
```
典型链路：`setModel` → `setText` → `setSelected` → `addActionListener` → `doClick` → `getActionListeners`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JButton.java`、`JToggleButton.java`、`JCheckBox.java`、`JRadioButton.java`、`JMenuItem.java` 全部 `extends AbstractButton`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/`）。
- JDK 源码 `javax/swing/plaf/basic/BasicButtonUI.java` 通过 `AbstractButton` 的 `getModel/setModel` 驱动按钮绘制与选择状态。
- JDK 测试 `test/jdk/javax/swing/AbstractButton/` 覆盖 `doClick` 与 mnemonic 行为。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract、父类 `JComponent`、实现 `ItemSelectable`+`SwingConstants`。
- 行为（黑盒）：`testBlackBox()` 覆盖默认 model 为 null、`setModel` 安装、文本/mnemonic/actionCommand 往返、`setSelected` 状态、margin 往返、外观开关、`setDisplayedMnemonicIndex`、`addActionListener`+`doClick` 触发一次动作事件。

## 5. 组件层深化批（Metal LaF + EDT）
`testContract()` 补充（JDK-8 侧镜像，双侧契约一致）：
- Metal 默认值锁定：对齐 CENTER/CENTER、textPosition TRAILING/CENTER、iconTextGap=4、
  margin=InsetsUIResource[2,14,2,14]、rolloverEnabled=true、borderPainted=true、
  focusPainted=true、multiClickThreshhold=0、displayedMnemonicIndex=-1。
- `setText(null)` → `getText()==null`（非 ""）；隐式 actionCommand 跟随 text，
  显式 setActionCommand 覆盖、`setActionCommand(null)` 恢复隐式。
- 4 个对齐 setter 传 99 抛 IllegalArgumentException；`setIconTextGap(-1)` 不抛（非对称）。
- setMnemonic 触发事件名 `"mnemonic"`；`setDisplayedMnemonicIndex(99)` 抛 IAE。
- **disabledIcon 派生只对 ImageIcon 生效**（查询时派生 GrayFilter 灰度副本），
  自定义 Icon 无派生；显式 setDisabledIcon 优先。
- 状态机对照：armed+pressed 时 selected=false；release 时 **ToggleButtonModel 翻转、
  DefaultButtonModel 不翻转**；`JToggleButton` 的 ItemEvent `getItem()==按钮本身`。
- setModel：不触发 ChangeEvent、handler 监听器迁移新模型、组件监听器保留。
- ButtonGroup：`setSelected(true)` 在组内切换（非拒绝）；remove 选中成员后 getSelection
  为 null 且该成员独立；clearSelection 全清。
- setAction：同步 text/tooltip/mnemonic/enabled，后续 Action 属性变化持续同步，
  doClick 转发 Action，`setAction(null)` 复位，hideActionText 抑制 text 同步。
