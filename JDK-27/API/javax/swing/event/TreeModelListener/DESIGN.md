# javax.swing.event.TreeModelListener 测试设计

## 1. API 功能

`TreeModelListener` 监听树模型（`TreeModel`）结构/节点变化，继承 `java.util.EventListener`，四个回调：

- `treeNodesChanged(TreeModelEvent e)`：节点内容变化。
- `treeNodesInserted(TreeModelEvent e)`：节点被插入。
- `treeNodesRemoved(TreeModelEvent e)`：节点被移除。
- `treeStructureChanged(TreeModelEvent e)`：结构整体变化。

事件 `TreeModelEvent` 携带 `getTreePath()`（`TreePath`）、`getPath()`（`Object[]` 路径）、`getChildIndices()`、`getChildren()`（均返回防御性副本）与 `getSource()`。构造器有 4 种重载（路径数组/TreePath × 带/不带子节点信息）。

## 2. Java 用户如何使用

构造：实现接口并注册到 `TreeModel`：

```java
DefaultTreeModel model = new DefaultTreeModel(root);
TreeModelListener l = new TreeModelListener() {
    public void treeNodesChanged(TreeModelEvent e)   { /* 节点变 */ }
    public void treeNodesInserted(TreeModelEvent e)  { /* 插入 */ }
    public void treeNodesRemoved(TreeModelEvent e)   { /* 删除 */ }
    public void treeStructureChanged(TreeModelEvent e) { /* 结构变 */ }
};
model.addTreeModelListener(l);
```

事件对象由 `DefaultTreeModel` 的 `fireTreeNodesChanged/fireTreeNodesInserted/fireTreeNodesRemoved/fireTreeStructureChanged` 构造（`new TreeModelEvent(this, path, childIndices, children)`）。

典型组合链路：`addTreeModelListener → insertNodeInto → treeNodesInserted（读 getTreePath/getChildIndices/getChildren）→ removeNodeFromParent → treeNodesRemoved → 修改节点 → treeNodesChanged`。

## 3. 开源代码实际用法调研

1. JDK `JTree` 持有 `protected transient TreeModelListener treeModelListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTree.java:337`），`createTreeModelListener()`（`:3848`）返回 `TreeModelHandler` 内部类，把模型变化转成视图刷新。
2. `DefaultTreeModel` 的 `fireTreeNodesInserted(...)` 等构造 `TreeModelEvent` 派发，是四种构造器与 `getTreePath/getChildIndices/getChildren` 的权威来源（`src/java.desktop/share/classes/javax/swing/tree/DefaultTreeModel.java`）。
3. `JTree` 在模型替换/结构变化时调用 `treeStructureChanged` 重建 UI（同文件多处）。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`TreeModelListenerTest.testBlackBox()`）：
- 匿名实现记录最近回调名与事件。
- 用路径数组构造 `TreeModelEvent` 触发 `treeNodesChanged`，断言 `getPath()` 内容、`getChildIndices()`/`getChildren()` 内容副本、`getTreePath()` 派生正确。
- 用 `TreePath` 构造 `treeNodesInserted/treeStructureChanged` 事件，断言 `getTreePath().equals(...)`、`getChildIndices()` 空数组、`getChildren()==null`（结构事件）。

白盒保留原有结构断言，新增四方法签名/抽象性与 `declared method count == 4`。
