# javax.swing.text.DefaultStyledDocument.ElementBuffer — DESIGN

## 1. API 功能
管理 `DefaultStyledDocument` 元素层级变更的类（`Serializable`）。方法分组：
- 构造/查询：`ElementBuffer(Element root)`、`getRootElement()`。
- 结构操作（需写锁）：`insert(int,int,ElementSpec[],DefaultDocumentEvent)`、`remove(int,int,DefaultDocumentEvent)`、`change(int,int,DefaultDocumentEvent)`。
- 克隆：`clone(Element parent, Element clonee)`。
- 辅助（protected/包内）：`insertUpdate/removeUpdate/changeUpdate/split/...`。

## 2. Java 用户如何使用
非 static 内部类，可 `doc.new ElementBuffer(root)` 构造，`getRootElement()` 取根；`clone(parent, clonee)` 复制整棵元素子树（需在文档写锁内调用，否则创建元素时抛 `StateInvariantError`）。正常文档操作由 `DefaultStyledDocument` 内部驱动。

## 3. 开源代码实际用法调研（来源：jdk8u 源码 + jdk/test）
- `jdk/src/share/classes/javax/swing/text/DefaultStyledDocument.java:87`：`buffer = new ElementBuffer(createDefaultRoot());`。
- `jdk/src/share/classes/javax/swing/text/DefaultStyledDocument.java:118`：`getDefaultRootElement()` 返回 `buffer.getRootElement()`。
- `jdk/src/share/classes/javax/swing/text/DefaultStyledDocument.java:216`：插入时 `buffer.insert(offset, length, data, evnt)`。

## 4. 测试用例设计 + 自动化实现
（15 断言）：ElementBuffer 经 `d.new ElementBuffer(root)` 构造（protected，子类暴露）；getRootElement 返回构造时的根（identity）；类身份/形状（insert/remove/change/clone 方法面）。反例：对 buffer 直接 clone() 在无初始化路径下抛 NPE（跳过）。
