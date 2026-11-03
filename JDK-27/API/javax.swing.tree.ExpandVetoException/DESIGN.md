# javax.swing.tree.ExpandVetoException 行为化测试设计

## 1. API 功能
在 `TreeWillExpandListener` 中阻止展开/折叠时抛出的受检异常，继承 `Exception`。
- 构造器：`ExpandVetoException(TreeExpansionEvent)`（无消息）、`ExpandVetoException(TreeExpansionEvent, String message)`。
- 继承方法：`getMessage()`、`toString()`、`getCause()`；受保护的 `event` 字段保存触发事件。

## 2. Java 用户如何使用
构造与多步链路：在监听器里 `new TreeExpansionEvent(tree, path)` → `throw new ExpandVetoException(event, "veto")`；调用方 `catch (ExpandVetoException e)` 后用 `e.getMessage()` 读取原因。

## 3. 开源代码实际用法调研
- JDK 源码 `javax.swing.JTree#fireTreeWillExpand(TreePath)`：构造 `TreeExpansionEvent` 并逐个通知 `TreeWillExpandListener.treeWillExpand(e)`，监听器可抛出 `ExpandVetoException` 阻止展开（`fireTreeWillExpand` 声明 `throws ExpandVetoException`）。
- JDK 源码 `javax.swing.JTree` 展开内部逻辑（约 3711/3738/3762 行）多处 `catch (ExpandVetoException eve)` 放弃展开/折叠。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `ExpandVetoExceptionTest.java`：
- 用 `TreeExpansionEvent` + 双参构造器断言 `getMessage()` 往返、`getCause()==null`。
- 单参构造器断言 `getMessage()==null`。
- 断言 `instanceof Exception/Throwable` 与 `toString()` 含类名。
