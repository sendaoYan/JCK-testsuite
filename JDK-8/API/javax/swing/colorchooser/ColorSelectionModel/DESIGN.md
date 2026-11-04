# javax.swing.colorchooser.ColorSelectionModel 测试设计（JDK-8 侧）

## 1. API 功能
颜色选择模型接口：恰好 4 个方法（getSelectedColor/setSelectedColor/addChangeListener/
removeChangeListener）；无 getChangeListeners（那是 DefaultColorSelectionModel 的具体方法）。

## 2. Java 用户如何使用
`JColorChooser(ColorSelectionModel)` 用自定义模型驱动选择器；监听 ChangeEvent 感知颜色变化。

## 3. 开源代码实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/colorchooser/ColorSelectionModel.java`；DefaultColorSelectionModel 为标准实现。

## 4. 测试用例设计 + 易错细节（headless 锁定，EDT）
- 反射：接口、方法集恰为 4（多一个少一个都 fail）、全部 abstract。
- **最小实现**（接口引用多态）：初始色、setColor 更新、监听器触发、事件 source==模型、
  同色不触发、remove 后不再通知、双监听器都收到。
- interop：自定义模型驱动 JColorChooser.getColor()，模型变化传播到 chooser。

自动化实现位于同目录 `ColorSelectionModelTest.java`（headless，无 @key）。
