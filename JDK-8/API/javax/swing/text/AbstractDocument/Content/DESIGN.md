# javax.swing.text.AbstractDocument.Content — DESIGN

## 1. API 功能
接口，抽象文档的字符存储层（`GapContent`、`StringContent` 是其标准实现）。方法分组：
- 容量：`length()`。
- 修改：`insertString(int,String)`、`remove(int,int)`（均返回 `UndoableEdit`）。
- 读取：`getString(int,int)`、`getChars(int,int,Segment)`。
- 位置：`createPosition(int)`（返回跟踪变更的 `Position`）。

## 2. Java 用户如何使用
通常由 `AbstractDocument` 内部持有，也可直接实例化实现类并按接口使用：`Content c = new GapContent(); c.insertString(0,"abc"); c.getString(0,c.length()); Position p = c.createPosition(1); c.remove(0,2);`。新建内容自带一个隐含换行符（`length()==1`）。

## 3. 开源代码实际用法调研（来源：jdk8u 源码 + jdk/test）
- `jdk/src/share/classes/javax/swing/text/AbstractDocument.java:455`：`getLength()` 返回 `data.length() - 1`，`getText` 委托 `data.getString`。
- `jdk/src/share/classes/javax/swing/text/PlainDocument.java:81`：`PlainDocument()` 使用 `new GapContent()`。
- `jdk/src/share/classes/javax/swing/text/DefaultStyledDocument.java:109`：`new GapContent(BUFFER_SIZE_DEFAULT)`。

## 4. 测试用例设计 + 自动化实现
ContentTest 以 GapContent 与 StringContent 双实现走查接口契约（48 断言）：
- 接口形状：length/insertString/remove/getString/getChars/createPosition 六方法。
- 共同契约：初始 length()==1 且唯一字符为隐式 "\n"；插入后用户内容在前、换行恒最后；中部插入精确；createPosition 后 remove 联动位置；getString/getChars 一致；remove 尾部换行/全部内容允许。
- 反例：getString/getChars 越界抛 BadLocationException；remove 越界抛 BLE（两实现的 offsetRequested 细节差异在各自用例中锁定）。
- insertString 返回 UndoableEdit：canUndo、undo 后内容回退、redo 恢复。
