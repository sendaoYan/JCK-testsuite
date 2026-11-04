# javax.swing.JSpinner 测试设计

## 1. API 功能
微调器（`extends JComponent`，implements `Accessible`）：SpinnerModel 驱动（Number/List/Date 三家）、
模型↔编辑器自动配对（NumberEditor/ListEditor/DateEditor）、ChangeEvent（值）与
PropertyChangeEvent("editor")（编辑器）双通道。

## 2. Java 用户如何使用
`new JSpinner()` 默认 SpinnerNumberModel(0,·,·,1) → `getNextValue/setValue` →
`addChangeListener`；表单里 `setModel(new SpinnerDateModel())` 后编辑器自动变 DateEditor。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JSpinner.java`
（setEditor 触发 "editor" PropertyChangeEvent；setModel 按需重建编辑器）；
`test/jdk/javax/swing/JSpinner/`。

## 4. 测试用例设计 + 易错细节
- 默认：value 0、next 1、prev −1、getNext/Previous 是纯查询不动值、
  默认编辑器 JSpinner$NumberEditor、BasicSpinnerUI、commitEdit 无异常。
- **步进不钳制**：模型 (5,-10,10,2) 下 setValue(9) 后 next==null（9+2=11 越界）、
  setValue(8) 后 next==10（恰好）——SpinnerNumberModel 只做 value+step 与边界比较。
- setValue("not-a-number") IAE 且值保持。
- null 矩阵：setModel(null) IAE；setEditor(null) IAE（message 含 "null editor"）。
- **事件分流**：setValue→ChangeEvent 1 次（同值 0）；setEditor(新)→恰好 1 次
  propertyName=="editor" 的 PCE（old=NumberEditor/new=自定义）且 0 次 ChangeEvent；
  setEditor(同)/setModel(同) 全静默。
- 类配对：Number→NumberEditor、List→ListEditor、Date→DateEditor；
  **setModel 换类型自动换编辑器**；SpinnerListModel 边界 "z" next null / prev "y"。

自动化实现位于同目录 `JSpinnerTest.java`；EDT 纪律 + Metal LaF 先行。
