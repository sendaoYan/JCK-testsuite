# java.awt.List 黑盒行为测试设计

## ① API 功能分组
- 构造：`List()`、`List(int rows)`、`List(int rows, boolean multipleMode)`。
- 条目管理：`add/addItem/insert/replaceItem/remove/removeAll/getItemCount/getItem/getItems`。
- 选择：`select/deselect/getSelectedIndex/getSelectedIndexes/getSelectedItem/getSelectedItems`。
- 模式：`setMultipleMode/isMultipleMode`、`getRows/makeVisible/getVisibleIndex`。
- 事件：`addItemListener/addActionListener` 及对应 getters。

## ② Java 用户用法
```java
List l = new List(4, true);
l.add("a"); l.add("b");
l.select(0); l.select(1);
l.getSelectedItems();
```
组合链路：构造（多选）→ add/insert 条目 → select 多项 → 读回选中 → remove/removeAll。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `List` 默认 `DEFAULT_VISIBLE_ROWS = 4`（`List.java`）。
- `List.add(String, int)` 在指定位置插入（`List.java`）。
- `List.getSelectedIndexes` 返回升序选中下标数组（`List.java`）。

## ④ 测试用例设计 + 自动化实现
- 默认行数/单选模式/空列表 → `testBlackBox`。
- 构造行数与多选模式 → `testBlackBox`。
- add/insert/replaceItem/remove/removeAll 条目管理 → `testBlackBox`。
- 单选 select/deselect + 多选 getSelectedIndexes/getSelectedItems → `testBlackBox`。
- makeVisible/getVisibleIndex + 事件监听管理 → `testBlackBox`。
- add 到显示 Frame 后 displayable → `testBlackBox`。
