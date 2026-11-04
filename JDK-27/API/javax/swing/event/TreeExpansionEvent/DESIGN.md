# TreeExpansionEvent 行为测试设计

## 1. API 功能
`javax.swing.event.TreeExpansionEvent` 是树节点展开/折叠事件，继承 `java.util.EventObject`。

- **构造器**：`TreeExpansionEvent(Object source, TreePath path)`。
- **查询**：`getPath()`（被展开/折叠的节点路径）、继承的 `getSource()`。

## 2. Java 用户如何使用
`JTree` 派发，用户实现 `TreeExpansionListener`：

```java
tree.addTreeExpansionListener(new TreeExpansionListener() {
    public void treeExpanded(TreeExpansionEvent e) {
        TreePath path = e.getPath();
        Object node = path.getLastPathComponent();
    }
    public void treeCollapsed(TreeExpansionEvent e) { }
});
```

每个方法典型调用：`new TreeExpansionEvent(tree, path)` 构造 → `getPath()` 取得路径 → `path.getPathCount()/getLastPathComponent()` 读取节点。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTree.java:2852/2862/2883/2892`：
   ```java
   e = new TreeExpansionEvent(this, path);
   ```
   `fireTreeWillExpand/fireTreeExpanded` 等内部方法统一用 `new TreeExpansionEvent(this, path)` 派发。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 EventObject、public、非 abstract、无接口）。
- 黑盒：构造双组件路径事件，验证 `getSource/getPath` 身份 round-trip 与 `TreePath` 组件内容；覆盖单组件路径（pathCount==1）。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
