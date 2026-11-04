# javax.swing.event.TreeWillExpandListener 测试设计

## 1. API 功能

`TreeWillExpandListener` 监听树节点**将要**展开/折叠（可拦截/否决），继承 `java.util.EventListener`，两个回调：

- `void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException`：节点将要展开。
- `void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException`：节点将要折叠。

事件 `TreeExpansionEvent` 携带 `getPath()`；监听器可通过抛出 `javax.swing.tree.ExpandVetoException` 阻止展开/折叠发生（veto 机制）。

## 2. Java 用户如何使用

构造：实现接口并注册到 `JTree`，需要时抛 `ExpandVetoException`：

```java
JTree tree = new JTree();
TreeWillExpandListener l = new TreeWillExpandListener() {
    public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
        // 如需阻止：throw new ExpandVetoException(e, "vetoed");
    }
    public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException { }
};
tree.addTreeWillExpandListener(l);
```

典型组合链路：`addTreeWillExpandListener → 用户展开节点 → treeWillExpand（可读 getPath；抛 ExpandVetoException 则取消展开）→ 未被否决才真正展开并触发 treeExpanded`。

## 3. 开源代码实际用法调研

1. JDK `JTree` 提供 `addTreeWillExpandListener` 并在展开前回调（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTree.java`，`fireTreeWillExpand` 路径）。
2. `JTree` 的 `fireTreeWillExpand`（同文件）逐个调用监听器，捕获 `ExpandVetoException` 以中止展开，是 veto 语义的实现处。
3. `ExpandVetoException`（`src/java.desktop/share/classes/javax/swing/tree/ExpandVetoException.java`）持有 `protected TreeExpansionEvent event`，构造 `ExpandVetoException(TreeExpansionEvent)`/`(event, message)`，是本测试抛出断言的依据。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`TreeWillExpandListenerTest.testBlackBox()`）：
- 普通匿名实现记录回调，构造 `TreeExpansionEvent`，调用 `treeWillExpand/treeWillCollapse`，断言回调触发、`getPath()` round-trip。
- veto 匿名实现抛 `ExpandVetoException`，断言调用时抛出 `ExpandVetoException`（且 `getCause()==null`），验证 veto 语义。

白盒保留原有结构断言，新增两方法返回 void、声明 `throws ExpandVetoException` 与 `declared method count == 2`。
