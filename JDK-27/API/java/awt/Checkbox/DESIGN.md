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

## ⑤ 契约深化（本轮追加）
- `Checkbox(null)` 保留 null label；默认 ("", false, 无组)。
- 组选择迁移：`setState(true)` 把组选择迁到本成员（旧成员翻 false）；第 2 个 true 构造进组会抢走选择。
- 历史行为锁定：对组内当前选中成员 `setState(false)` 是彻底 no-op（自身 state 保持 true、组引用保持）——两个 JDK 实测一致。
- `setSelectedCheckbox(null)` 把成员 state 翻 false；detach（setCheckboxGroup(null)）保留自身 state 并清除组选择。
- `getSelectedObjects()` 选中时返回含自身引用的数组、未选中返回 null。
- 同一 ItemListener 注册两次计数 2；程序化 setState 不派发 ItemEvent。
