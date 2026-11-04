# java.awt.CheckboxMenuItem 黑盒行为测试设计

## ① API 功能分组
- 构造：`CheckboxMenuItem()`、`CheckboxMenuItem(String)`、`CheckboxMenuItem(String, boolean)`。
- 状态：`getState/setState/getSelectedObjects`。
- 事件：`addItemListener/removeItemListener/getItemListeners`。
- 继承 MenuItem：`getLabel/setLabel/isEnabled/setEnabled`。

## ② Java 用户用法
```java
CheckboxMenuItem c = new CheckboxMenuItem("Wrap", true);
c.setState(false);
menu.add(c);
```
组合链路：构造（含初始状态）→ setState → getSelectedObjects → 添加到 Menu。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `CheckboxMenuItem` 默认 label=""、state=false（`CheckboxMenuItem.java`）。
- `CheckboxMenuItem.getSelectedObjects` 仅当 state 为 true 时返回标签数组（`CheckboxMenuItem.java`）。
- `CheckboxMenuItem.setState` 更新 state 并触发 ItemEvent（`CheckboxMenuItem.java`）。

## ④ 测试用例设计 + 自动化实现
- 默认标签/状态 → `testBlackBox`。
- 构造初始状态 + setState 往返 → `testBlackBox`。
- getSelectedObjects 有无选中 → `testBlackBox`。
- 继承 setLabel/setEnabled + ItemListener 管理 → `testBlackBox`。
