# TreeModelEvent 行为测试设计

## 1. API 功能
`javax.swing.event.TreeModelEvent` 是树模型结构/数据变更事件，继承 `java.util.EventObject`。

- **构造器**：四组——`(Object,Object[] path,int[] childIndices,Object[] children)`、`(Object,TreePath path,int[] childIndices,Object[] children)`、`(Object,Object[] path)`、`(Object,TreePath path)`。
- **查询**：`getTreePath()`（TreePath 形式）、`getPath()`（Object[] 形式，path 为 null 时返回 null）、`getChildren()`（防御性拷贝，children 为 null 时返回 null）、`getChildIndices()`（防御性拷贝，为 null 时返回 null）、`toString()`。
- **默认值**：`(Object,TreePath)` 构造器会把 `childIndices` 设为空 `int[0]`，`children` 保持 null；`(Object,Object[])` 会内部 `new TreePath(path)`。

## 2. Java 用户如何使用
`TreeModel` 实现派发，用户实现 `TreeModelListener`：

```java
model.addTreeModelListener(new TreeModelListener() {
    public void treeNodesInserted(TreeModelEvent e) {
        TreePath tp = e.getTreePath();
        Object[] path = e.getPath();
        int[] idx = e.getChildIndices();
        Object[] children = e.getChildren();
    }
    ...
});
```

每个方法典型调用：`getTreePath()` 定位父路径 → `getChildIndices()` 定位子索引 → `getChildren()` 取得变更的子节点。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/tree/FixedHeightLayoutCache.java` 内部消费 `TreeModelEvent` 的 `getTreePath/getChildIndices/getChildren` 做增量布局更新。
2. `jdk25u-dev/src/jdk.hotspot.agent/.../treetable/AbstractTreeTableModel.java` 使用 `TreeModelEvent` 派发树表结构变化。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 EventObject、public、非 abstract、无接口）。
- 黑盒：用 Object[] 路径构造器验证内部 TreePath 构建与 `getPath/getTreePath` round-trip；验证 `getChildIndices/getChildren` 值与防御性拷贝；验证 TreePath 单参构造器的空 indices/null children 默认值；验证 Object[] 单参构造器 indices==null；断言 `toString()` 非空。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。

## 5. 深化追加（第二轮）
- 构造器全重载补缺：`(TreePath, int[], Object[])`——TreePath 身份保持、getPath/children/indices round-trip。
