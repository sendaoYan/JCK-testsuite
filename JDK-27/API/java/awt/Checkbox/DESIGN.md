# java.awt.Checkbox 黑盒行为测试设计

## ① API 功能分组
- 构造：`Checkbox()`、`Checkbox(String)`、`Checkbox(String, boolean)`、`Checkbox(String, boolean, CheckboxGroup)`、`Checkbox(String, CheckboxGroup, boolean)`。
- 标签/状态：`getLabel/setLabel`、`getState/setState`。
- 选择对象：`getSelectedObjects`。
- 分组：`getCheckboxGroup/setCheckboxGroup`。
- 事件：`addItemListener/removeItemListener/getItemListeners`。

## ② Java 用户用法
```java
Checkbox c = new Checkbox("Enable", true);
c.setState(false);
CheckboxGroup g = new CheckboxGroup();
Checkbox a = new Checkbox("a", false, g);
```
组合链路：构造（含组）→ 标签/状态 setter → 选中对象 → add 到显示 Frame。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Checkbox` 默认 label=""、state=false（`Checkbox.java`）。
- `Checkbox.getSelectedObjects` 仅当 state 为 true 时返回 label 数组（`Checkbox.java`）。
- `Checkbox` 构造时若 state 为 true 则设置 `CheckboxGroup.selectedCheckbox`（`Checkbox.java`）。

## ④ 测试用例设计 + 自动化实现
- 默认 label/state/group → `testBlackBox`。
- setLabel/setState 往返 + getSelectedObjects 有无选中 → `testBlackBox`。
- ItemListener 管理 → `testBlackBox`。
- CheckboxGroup 分配与 setCheckboxGroup(null) → `testBlackBox`。
- add 到显示 Frame 后 displayable → `testBlackBox`。
