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

## ⑤ 契约深化（本轮追加）
- 空态契约：count 0、selectedIndex -1、selectedItem null。
- `add(null)`/`addItem(null)` 抛精确 NullPointerException；`add(String)` 被接受。
- 首个 add 自动把选中态置为 index 0（后续 add 不改）。
- `getItem(99)`/`getItem(-1)` AIOOBE；`insert(x,-1)` IAE；insert 在 count 处或越界均追加（不抛）；insert(0) 移位。
- `remove(99)`/`remove(-1)` AIOOBE；`remove(String 不存在或 null)` 抛 IAE（不是 NoSuchElementException）。
- `select(99)`/`select(-1)` IAE 且失败后保持原选择；`select(String 不存在/null)` 静默 no-op。
- removeAll 清空 items 且清空 selection（index 回 -1）；ItemListener 登记/移除对称。
