# java.awt.TextArea 黑盒行为测试设计

## ① API 功能分组
- 常量：`SCROLLBARS_BOTH/VERTICAL_ONLY/HORIZONTAL_ONLY/NONE`。
- 构造：`TextArea()`、`TextArea(String)`、`TextArea(int, int)`、`TextArea(String, int, int)`、`TextArea(String, int, int, int)`。
- 编辑：`insert/append/replaceRange`。
- 行列：`getRows/setRows/getColumns/setColumns/getScrollbarVisibility`。
- 文本/选择（继承 TextComponent）：`setText/select/selectAll/getSelectedText/setCaretPosition/getCaretPosition/setEditable/isEditable`。

## ② Java 用户用法
```java
TextArea t = new TextArea("hello", 5, 40, TextArea.SCROLLBARS_BOTH);
t.append("\nworld");
t.replaceRange("x", 0, 5);
```
组合链路：构造（含行列/滚动条）→ append/insert/replaceRange 编辑 → 选择/光标组合 → add 到显示 Frame。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `TextArea` 默认 rows=0、columns=0、滚动条 BOTH（`TextArea.java`）。
- `TextArea.append` 委托 `insert(text, end)`（`TextArea.java`）。
- `TextArea.replaceRange` 更新文本并同步 peer（`TextArea.java`）。

## ④ 测试用例设计 + 自动化实现
- 常量精确值 + 默认行列/滚动条 → `testBlackBox`。
- append/insert/replaceRange 编辑结果 → `testBlackBox`。
- 选择/全选/光标/可编辑 组合 → `testBlackBox`。
- getPreferredSize/getMinimumSize 非空 → `testBlackBox`。
- add 到显示 Frame 后 displayable → `testBlackBox`。

## ⑤ 契约深化（本轮追加）
- rows/columns 默认 0；`setRows(0)`/`setColumns(0)` 允许（不抛），负数抛 IAE。
- null 文本语义：`append(null)`/`insert(null,i)`/`replaceRange(null,i,j)` 写入字面 4 字符 "null"；而 `setText(null)` 归一化为空串（同一类内两套 null 语义并存，实测锁定）。
- `replaceRange(str, 4, 2)` 交换边界不抛：按 `substring(0,start)+str+substring(end)` 拼接得到 "abcdXYcdef" 式结果（end 只决定尾巴起点）。
- insert 越界与 replaceRange 常规越界抛 StringIndexOutOfBoundsException；insert(str,3)/append 往返精确断言。
