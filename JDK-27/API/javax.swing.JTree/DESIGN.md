# javax.swing.JTree — DESIGN

## 1. API 功能
树（`extends JComponent`，implements `Scrollable/Accessible`）。核心方法分组：
- 模型：`setModel/getModel`（TreeModel，DefaultTreeModel/DefaultMutableTreeNode 驱动）、`getRowCount`、`isRootVisible/setRootVisible`。
- 展开/折叠：`expandPath/collapsePath`、`isExpanded/isCollapsed`、`hasBeenExpanded`、`isVisible(TreePath)`。
- 行/路径：`getRowForPath/getPathForRow`。
- 选择：`setSelectionPath/getSelectionPath/getSelectionCount/getSelectionRows/getMinSelectionRow/isPathSelected/getLastSelectedPathComponent/getLeadSelectionPath`、`setSelectionRow`、`clearSelection/isSelectionEmpty`、`setSelectionModel/getSelectionModel`。
- 监听：`addTreeExpansionListener`、`addTreeSelectionListener`、TreeModelListener（内部保持行同步）。

## 2. Java 用户如何使用
`DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");` 添加子节点 → `new DefaultTreeModel(root)` → `new JTree(model)` → `expandPath/collapsePath` 控制展开 → `setSelectionPath` 选中 → `getRowForPath/getPathForRow` 行路径互转 → `model.insertNodeInto(...)` 增删节点（树自动同步）。`setRootVisible(false)` 隐藏根行。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTree.java`：`setModel` 时 `expandedState.put(new TreePath(root), TRUE)` 标记根展开；`isVisible(root)` 恒 true（根特例）；`getRowCount` 依赖展开状态。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JTree/`：展开/折叠监听器序列、选择、模型插入同步断言。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/tree/DefaultTreeModel.java`：`insertNodeInto` 触发 nodesInserted 事件。

## 4. 测试用例设计（黑盒部分，`JTreeTest.testBlackBox`）
1. 固定 DefaultTreeModel：model 同实例、root 默认可见/展开、rowCount==4、child 折叠。
2. collapsePath/expandPath 根：rowCount 1↔4、isCollapsed/isVisible、展开监听器记录 "col:root","exp:root"。
3. 嵌套展开 c1：rowCount 6、getRowForPath/getPathForRow 往返、collapsePath 记录序列。
4. setRootVisible(false)：rowCount 3、首行是 c1、isVisible(root) 恒 true。
5. 选择：setSelectionPath 触发 TreeSelectionListener、lead path 组件 == c2、selection rows/count/min/isPathSelected/last component；setSelectionRow/clearSelection。
6. 选择模型替换后监听仍触发；model.insertNodeInto 触发 TreeModelListener + 行数增长 + 新节点可见；setModel 清空选择 + 新根自动展开 + setModel(null) 空行。
