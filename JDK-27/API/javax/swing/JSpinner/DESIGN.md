# javax.swing.JSpinner 测试设计

## 1. API 功能
- 构造器：`JSpinner()`（默认 `SpinnerNumberModel`，值 0、无上下界、步长 1）、`JSpinner(SpinnerModel)`（model 为 null 抛 NPE）。
- 模型驱动：`getModel/setModel`、`getValue/setValue`、`getNextValue/getPreviousValue`（越界返回 null）。
- 编辑器：`getEditor/setEditor`、`commitEdit()`（抛 `ParseException`）。
- UI：`getUI/setUI/updateUI/getUIClassID`。

## 2. Java 用户如何使用
典型组合：`new JSpinner(new SpinnerNumberModel(5, 0, 10, 2))` → `getValue()` 取当前值 → `getNextValue()`/`getPreviousValue()` 计算步进；`setValue(...)` 回填；`setModel` 换模型后 `getEditor()` 获得配套编辑器；表单提交前 `commitEdit()`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/swing/JSpinner/bug4680204.java`：`new JSpinner(new SpinnerNumberModel(1, 1, 100, 1))` 与 `sp2.setModel(new SpinnerNumberModel(1, 1, 100, 1))` 验证模型替换与值。
- JDK 源码 `javax.swing.JSpinner.setModel`：替换模型并按需 `createEditor` 重建编辑器，说明 model→editor 联动。
- 常见表单用 `SpinnerNumberModel(min, max, step)` 做数值输入并配 `commitEdit()` 处理非法输入。

## 4. 测试用例设计 + 自动化实现
黑盒覆盖：默认模型值 0、next=1、previous=-1；`SpinnerNumberModel` 驱动 getValue/getNextValue/getPreviousValue（含边界返回 null）；`setValue` 数值与非数值（非数值抛 `IllegalArgumentException`）；`setModel/getModel` 联动值；`setEditor/getEditor` 往返；`commitEdit()` 无异常；默认编辑器为 `JSpinner.DefaultEditor`。见 `JSpinnerTest.java` 的 `testBlackBox()`。


## 5. 深化部分（`JSpinnerTest.testDeepContracts`，编辑器属性事件 + 边界步进 + 类配对）
1. null 拒绝矩阵：setModel(null) IAE；setEditor(null) IAE 且 message 含 "null editor"。
2. 事件分流：setValue 触发 ChangeEvent 1 次（同值 0 次）；setEditor(新) 触发恰好 1 次 propertyName=="editor" 的 PropertyChangeEvent（old=NumberEditor、new=自定义）且不触发 ChangeEvent；setEditor(同)/setModel(同) 均 0 事件。
3. 步进语义：value+step **不钳制**——模型 (5,-10,10,2) 下 setValue(9) 后 getNextValue()==null（9+2=11 越界）、setValue(8) 后 getNextValue()==10（恰好）。
4. 模型↔编辑器类配对：NumberModel→NumberEditor、DateModel→DateEditor、ListModel→ListEditor；换模型自动换编辑器类。SpinnerListModel 边界："z" next null、prev "y"。
5. UI 绑定：BasicSpinnerUI（Metal 亦复用）/ getUIClassID=="SpinnerUI"。
