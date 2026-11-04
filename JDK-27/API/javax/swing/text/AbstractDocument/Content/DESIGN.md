# javax.swing.text.AbstractDocument.Content — DESIGN

## 1. API 功能
接口，抽象文档的字符存储层（`GapContent`、`StringContent` 是其标准实现）。方法分组：
- 容量：`length()`。
- 修改：`insertString(int,String)`、`remove(int,int)`（均返回 `UndoableEdit`）。
- 读取：`getString(int,int)`、`getChars(int,int,Segment)`。
- 位置：`createPosition(int)`（返回跟踪变更的 `Position`）。

## 2. Java 用户如何使用
通常由 `AbstractDocument` 内部持有，也可直接实例化实现类并按接口使用：`Content c = new GapContent(); c.insertString(0,"abc"); c.getString(0,c.length()); Position p = c.createPosition(1); c.remove(0,2);`。新建内容自带一个隐含换行符（`length()==1`）。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java:455`：`getLength()` 返回 `data.length() - 1`，`getText` 委托 `data.getString`。
- `src/java.desktop/share/classes/javax/swing/text/PlainDocument.java:81`：`PlainDocument()` 使用 `new GapContent()`。
- `src/java.desktop/share/classes/javax/swing/text/DefaultStyledDocument.java:109`：`new GapContent(BUFFER_SIZE_DEFAULT)`。

## 4. 测试用例设计 + 自动化实现（`ContentTest.testBlackBox`）
- 以 `GapContent` 为主走 `length/getString/insertString/remove/getChars/createPosition` 全链路。
- 位置跟踪：在位置前插入，位置右移。
- 边界：负偏移插入、越界 `getString/remove` 抛 `BadLocationException`。
- 用 `StringContent` 验证同一契约（初始长度、insert/getString、位置跟踪）。
