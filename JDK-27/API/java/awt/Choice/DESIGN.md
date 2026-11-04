# java.awt.Choice 黑盒行为测试设计

## ① API 功能分组
- 构造：`Choice()`。
- 条目管理：`add/addItem/insert/remove/removeAll/getItemCount/getItem`。
- 选择：`select(int)/select(String)/getSelectedIndex/getSelectedItem/getSelectedObjects`。
- 事件：`addItemListener/removeItemListener/getItemListeners`。

## ② Java 用户用法
```java
Choice c = new Choice();
c.add("A"); c.add("B");
c.select(0);
c.getSelectedItem();
```
组合链路：构造 → add/insert 条目 → select → 读回选中项/对象 → remove/removeAll。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Choice` 初始 `selectedIndex = -1`（`Choice.java`）。
- `Choice.select(int)` 同步 `selectedIndex` 并触发 ItemEvent（`Choice.java`）。
- `Choice.remove` 会调整 `selectedIndex`（`Choice.java`）。

## ④ 测试用例设计 + 自动化实现
- 初始空/无选中 → `testBlackBox`。
- add/addItem/insert 条目顺序 → `testBlackBox`。
- select(index/string) + 选中项/对象读回 → `testBlackBox`。
- remove(string/int)/removeAll 及选中清除 → `testBlackBox`。
- ItemListener 管理 → `testBlackBox`。
- add 到显示 Frame 后 displayable → `testBlackBox`。
