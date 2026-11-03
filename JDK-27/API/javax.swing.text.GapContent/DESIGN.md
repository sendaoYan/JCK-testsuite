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
