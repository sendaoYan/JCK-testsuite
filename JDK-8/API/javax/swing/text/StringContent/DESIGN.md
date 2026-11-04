# javax.swing.text.StringContent — DESIGN

## 1. API 功能
`AbstractDocument.Content` 的简单字符数组实现（`final` 类）。方法分组：
- 构造：`StringContent()`（默认 10）、`StringContent(int)`（小于 1 则取 1）。
- 容量/修改：`length()`、`insertString(int,String)`、`remove(int,int)`（返回 `UndoableEdit`）。
- 读取：`getString(int,int)`、`getChars(int,int,Segment)`。
- 位置：`createPosition(int)`。

## 2. Java 用户如何使用
`new StringContent()`（初始 `length()==1` 含隐含换行）→ `insertString(0,"abc")`（只能插在 `< length()` 处）→ `getString(0,length())` → `createPosition(1)` 跟踪 → `remove(0,3)`；插入/删除返回的 `UndoableEdit` 可 `undo()/redo()`。

## 3. 开源代码实际用法调研（来源：jdk8u 源码 + jdk/test）
- `jdk/src/share/classes/javax/swing/text/StringContent.java:53`：类定义，`public final class StringContent implements AbstractDocument.Content, Serializable`。
- `jdk/src/share/classes/javax/swing/text/StringContent.java:96`：`insertString` 在 `where >= count` 时抛 `BadLocationException`。
- 文档注释指出它适合小文档/调试，通常推荐 gap buffer 或 piece table 实现。

## 4. 测试用例设计 + 自动化实现
`testInitial/testInsert/testInsertBounds/testRemove/testGetStringGetChars/testPositions/testUndoRedo/testSerialization`（50 断言）：
- 初始态与尺寸钳制（<1/负 → 1）；隐式换行语义。
- 插入：0/中部/length()-1 合法；insert 于 length() 允许（尾接）；负/越界 → BLE 且 offsetRequested 精确。
- 删除：负偏移 len>0 → BLE；len==0 越界 no-op；删除尾部换行允许。
- getString/getChars 越界/负偏移 BLE 精确；Segment 填充正确。
- 位置：插入点前移/后不动、塌缩到删除起点、0 钉死、insert at 0 保持既有 mark 在 0。
- Undo/Redo：insert/remove 编辑往返与位置联动。
- 序列化 round-trip：内容/可变性保留。
