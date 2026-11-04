# javax.swing.ButtonModel — DESIGN

## 1. API 功能
接口（`javax.swing.ButtonModel extends java.awt.ItemSelectable`），按钮（JButton/JToggleButton/JRadioButton/JCheckBox/JMenuItem）的状态机模型：
- 五个状态位查询/翻转：`isArmed/isSelected/isEnabled/isPressed/isRollover` + 对应 `setXxx`
- 标识：`setMnemonic/getMnemonic`、`setActionCommand/getActionCommand`、`setGroup/getGroup`（getGroup 为 default 方法，返回 null）
- 监听：`add/removeChangeListener`、`add/removeActionListener`、`add/removeItemListener`（继承 ItemSelectable）
- `getSelectedObjects()`（ItemSelectable 契约；DefaultButtonModel 固定返回 null）
- 参考实现：`javax.swing.DefaultButtonModel`（默认 enabled，其余 false）

## 2. Java 用户如何使用
- 构造：`new DefaultButtonModel()`；或经 `JButton.setModel(new DefaultButtonModel())`（JDK 中 JButton 即如此）。
- 典型链路（UI 委托模拟点击）：`setEnabled(true)` → `setArmed(true)` → `setPressed(true)` → `setPressed(false)`（armed+release → `ActionListener` 收到 ActionEvent，携带 actionCommand）→ `setRollover(true/false)` 纯视觉提示。
- 单选语义：`ButtonGroup.add(button)` 内部调 `model.setGroup(group)`；用户经 `group.setSelected(model, true)` 互斥选择；状态变化发 ChangeEvent，选择变化另发 ItemEvent(SELECTED/DESELECTED)。
- 禁用语义（读 jdk25u `DefaultButtonModel.java`）：`setEnabled(false)` 同时清 armed+pressed，且此后 setArmed/setPressed/setRollover 均为 no-op；同值设置不触发任何事件。

## 3. 开源用法调研
WebSearch 在本环境不可靠，改用可验证的 JDK 源码/自带测试并标注来源：
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JButton.java`（L131）：`setModel(new DefaultButtonModel())`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JToggleButton.java`（L327）：`ToggleButtonModel extends DefaultButtonModel`，覆盖 `setPressed` 实现点击切换 selected —— 展示按状态位扩展模型的惯用法。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicButtonListener.java`：鼠标进出调 `setRollover/setArmed/setPressed`，即 armed/pressed/rollover 状态机的真实驱动方。
- `~/git/jdk25u-dev/test/jdk/javax/swing/AbstractButton/`（bug4290656.java 等）：围绕 model 状态与事件回归。

## 4. 测试用例设计（黑盒部分，`ButtonModelTest.testBlackBox`）
以接口类型 `ButtonModel` 持有 `DefaultButtonModel`，配三个记录监听器（Change/Item/Action）与一个零 L&F 的 `TestButton extends AbstractButton`（仅 setModel）：
1. 八个默认值断言。
2. armed/pressed/rollover/mnemonic 逐一翻转 + ChangeEvent 计数；同值 no-op。
3. actionCommand 往返。
4. setSelected：ItemEvent SELECTED（source/itemSelectable 精确）+ ChangeEvent；重复 no-op；DESELECTED。
5. armed+release → ActionEvent（actionCommand、source 精确）。
6. setEnabled(false)：清 armed/pressed、屏蔽三 setter、再启用恢复；getSelectedObjects()==null。
7. ButtonGroup 链路：add 后 getGroup 同实例；group.setSelected 互斥（m1→m2）；clearSelection。
断言合计 37 条。
