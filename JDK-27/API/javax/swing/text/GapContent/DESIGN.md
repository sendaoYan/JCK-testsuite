# javax.swing.text.GapContent — DESIGN

## 1. API 功能
`AbstractDocument.Content` 的 gap-buffer（缺口缓冲）实现，继承 `GapVector`。方法分组：
- 构造：`GapContent()`（默认 10）、`GapContent(int)`（至少 2）。
- 容量/修改：`length()`、`insertString(int,String)`、`remove(int,int)`（返回 `UndoableEdit`）。
- 读取：`getString(int,int)`、`getChars(int,int,Segment)`。
- 位置：`createPosition(int)`。
- 数组支撑（protected）：`allocateArray(int)`、`getArrayLength()`。

## 2. Java 用户如何使用
`new GapContent()`（初始 `length()==1` 含隐含换行）→ `insertString(0,"abc")` → `getString(0,length())` → `createPosition(1)` 跟踪 → `remove(0,2)`；插入/删除返回的 `UndoableEdit` 可 `undo()/redo()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/PlainDocument.java:81`：`PlainDocument()` 使用 `new GapContent()`。
- `src/java.desktop/share/classes/javax/swing/text/DefaultStyledDocument.java:109`：`new GapContent(BUFFER_SIZE_DEFAULT)`。
- `src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:311`：`new GapContent(BUFFER_SIZE_DEFAULT)` 作为 HTML 文档存储。

## 4. 测试用例设计 + 自动化实现（`GapContentTest.testBlackBox`）
- 初始长度/换行；insert/getString/getChars 往返；位置跟踪（位置前插入右移）。
- 插入返回的编辑 `undo()/redo()`；删除返回的编辑 `undo()/redo()`。
- `GapContent(int)` 构造可用；边界：负偏移插入、`remove(0,length())`、越界 `getString` 抛 `BadLocationException`。

## 5. 契约深化（本轮追加）
`testInitial/testInsert/testRemove/testGetStringGetChars/testPositions/testUndoRedo/testSerialization`（70 断言）：
- 初始态：length()==1（隐式换行）、位置 0 处即 "\n"；构造器尺寸 <1 或负数一律钳到 1。
- 插入：0 / 中部 / 用户内容末尾（length()-1 处）精确；空串 no-op；GapContent 允许 insertString 于 length()（where==length 合法）；越界/负偏移 → BadLocationException 且 offsetRequested 精确（越界=length、负=length+1）。
- 删除：前部/尾部/删除尾部隐式换行（允许，内容回到裸 "\n"）；越界 BLE 精确；负偏移 len>0 → BLE(length+1)、len==0 时 no-op（不抛）。
- getString/getChars：越界/负偏移 BLE 精确；getChars 填充 Segment（count/内容）且 length==0 容错；Segment 部分返回语义。
- 位置：createPosition 各点偏移精确；插入前移/后不动、删除区间内塌缩到删除起点、0 钉死；同偏移位置 intern 为同一实例；越界偏移原样存储（createPosition 不校验）。
- Undo/Redo：insertString 返回可 undo 的编辑（新编辑 canUndo 不能 redo）；undo 后回到裸换行、redo 恢复；remove 编辑的 undo/redo 同步调整既有位置；fresh edit 直接 redo 抛 CannotRedoException。
- 序列化 round-trip：内容与 length 精确保留。
