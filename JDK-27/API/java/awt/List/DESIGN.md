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

## ⑤ 契约深化（本轮追加）
- `List(0)` 行数回退 4（负数 rows 同样接受）；`List(6)` 单选、`List(2,true)` 多选。
- `add(null)` 接受（与 Choice 相反）；`add(item,-1)` 与越界 index 均按追加处理；`add(item,1)` 精确插入；`replaceItem(null,0)` 接受。
- 越界：`replaceItem(x,99)`/`getItem(99)`/`remove(99)`/`remove(-2)` 均精确 AIOOBE；`remove("不存在")` 抛 IAE。
- 越界 select 的原始索引泄漏（历史怪癖锁定）：`select(-1)` 后 getSelectedIndex()==-1、`select(99)` 后 getSelectedIndex()==99（原样存储越界值），两种状态下 `getSelectedItems()` 都抛 AIOOBE；deselect 越界不修正索引；`makeVisible(-1)` no-op。
- 单选 reselect 迁移选择、deselect 清空；`setMultipleMode(false)` 保留已有多选但 getSelectedIndex() 报 -1。
