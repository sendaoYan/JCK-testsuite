# java.awt.TextArea 行为测试设计

## 1. API 功能
构造：`TextArea()`/`(String)`/`(rows,columns)`/`(String,rows,columns,scrollbars)`，常量 SCROLLBARS_BOTH=0/VERTICAL_ONLY=1/HORIZONTAL_ONLY=2/NONE=3；文本：`append/insert(String,int)/replaceRange(String,int,int)`；rows/columns：`getRows/setRows`、`getColumns/setColumns`；继承 TextComponent 的 getText/setText/select/caret/editable。

## 2. Java 用户如何使用
```java
TextArea ta = new TextArea("abc", 4, 10, TextArea.SCROLLBARS_VERTICAL_ONLY);
ta.append("DEF");
```
构造 → 常量 → rows/columns → 文本编辑矩阵 → 边界反例。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/TextArea.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractTextArea()`）。
对应 `TextAreaTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- rows/columns 默认 0；`setRows(0)`/`setColumns(0)` 允许（不抛），负数抛 IAE。
- null 文本语义：`append(null)`/`insert(null,i)`/`replaceRange(null,i,j)` 写入字面 4 字符 "null"；而 `setText(null)` 归一化为空串（同一类内两套 null 语义并存，实测锁定）。
- `replaceRange(str, 4, 2)` 交换边界不抛：按 `substring(0,start)+str+substring(end)` 拼接得到 "abcdXYcdef" 式结果（end 只决定尾巴起点）。
- insert 越界与 replaceRange 常规越界抛 StringIndexOutOfBoundsException；insert(str,3)/append 往返精确断言。
