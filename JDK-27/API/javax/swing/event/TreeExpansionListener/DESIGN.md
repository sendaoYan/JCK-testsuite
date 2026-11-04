# javax.swing.event.TreeExpansionListener 测试设计

## 1. API 功能

`TreeExpansionListener` 监听树节点展开/折叠，继承 `java.util.EventListener`，两个回调：

- `treeExpanded(TreeExpansionEvent e)`：节点已展开。
- `treeCollapsed(TreeExpansionEvent e)`：节点已折叠。

事件 `TreeExpansionEvent` 携带 `getPath()`（`TreePath`，展开/折叠的节点路径）与 `getSource()`。

## 2. Java 用户如何使用

构造：实现接口并注册到 `JTree`：

```java
JTree tree = new JTree();
TreeExpansionListener l = new TreeExpansionListener() {
    public void treeExpanded(TreeExpansionEvent e)  { /* 已展开 */ }
    public void treeCollapsed(TreeExpansionEvent e) { /* 已折叠 */ }
};
tree.addTreeExpansionListener(l);
```

事件对象由 `JTree` 内部构造 `new TreeExpansionEvent(tree, path)` 分发。

典型组合链路：`addTreeExpansionListener → 展开节点 → treeExpanded（读 e.getPath().getLastPathComponent()）→ 折叠 → treeCollapsed`。

## 3. 开源代码实际用法调研

1. JDK `JTree` 提供 `addTreeExpansionListener` 并在展开/折叠后触发 `TreeExpansionEvent`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTree.java:506,757` 区域 `uiTreeExpansionListener` 相关）。
2. `JTree` 的 `fireTreeExpanded/fireTreeCollapsed`（同文件）构造 `new TreeExpansionEvent(this, path)` 派发，是事件构造来源。
3. `TreeExpansionEvent` 源码（`src/java.desktop/share/classes/javax/swing/event/TreeExpansionEvent.java`）持有 `protected TreePath path`，`getPath()` 直接返回，是本测试 round-trip 依据。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`TreeExpansionListenerTest.testBlackBox()`）：
- 匿名实现记录最近回调名与事件。
- 构造 `TreePath`（两段），构造两个 `TreeExpansionEvent`，分别调用 `treeExpanded/treeCollapsed`。
- 断言回调名正确、事件实例一致、`getSource()`/`getPath()` round-trip、`getPath().getPathCount()==2`。

白盒保留原有结构断言，新增两方法签名/抽象性与 `declared method count == 2`。
