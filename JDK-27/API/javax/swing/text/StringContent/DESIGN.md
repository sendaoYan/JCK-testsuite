# javax.swing.text.StringContent — DESIGN

## 1. API 功能
`AbstractDocument.Content` 的简单字符数组实现（`final` 类）。方法分组：
- 构造：`StringContent()`（默认 10）、`StringContent(int)`（小于 1 则取 1）。
- 容量/修改：`length()`、`insertString(int,String)`、`remove(int,int)`（返回 `UndoableEdit`）。
- 读取：`getString(int,int)`、`getChars(int,int,Segment)`。
- 位置：`createPosition(int)`。

## 2. Java 用户如何使用
`new StringContent()`（初始 `length()==1` 含隐含换行）→ `insertString(0,"abc")`（只能插在 `< length()` 处）→ `getString(0,length())` → `createPosition(1)` 跟踪 → `remove(0,3)`；插入/删除返回的 `UndoableEdit` 可 `undo()/redo()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/StringContent.java:53`：类定义，`public final class StringContent implements AbstractDocument.Content, Serializable`。
- `src/java.desktop/share/classes/javax/swing/text/StringContent.java:96`：`insertString` 在 `where >= count` 时抛 `BadLocationException`。
- 文档注释指出它适合小文档/调试，通常推荐 gap buffer 或 piece table 实现。

## 4. 测试用例设计 + 自动化实现（`StringContentTest.testBlackBox`）
- 初始长度/换行；insert/getString/getChars 往返；位置跟踪（位置前插入右移）。
- 插入/删除返回的编辑 `undo()/redo()`。
- `StringContent(0)` 长度钳制为 1；边界：`insertString(length(),...)`（与 GapContent 不同的边界）、负偏移、`remove(0,length())`、越界 `getString` 均抛 `BadLocationException`。
