# TreeSelectionEvent 行为测试设计

## 1. API 功能
`javax.swing.event.TreeSelectionEvent` 是树选择事件，继承 `java.util.EventObject`。

- **构造器**：`(Object,TreePath[] paths,boolean[] areNew,TreePath oldLead,TreePath newLead)` 与单路径便捷构造器 `(Object,TreePath path,boolean isNew,TreePath oldLead,TreePath newLead)`。
- **查询**：`getPaths()`（防御性拷贝）、`getPath()`（第一个路径）、`isAddedPath()`/`isAddedPath(int)`/`isAddedPath(TreePath)`、`getOldLeadSelectionPath()`、`getNewLeadSelectionPath()`、`cloneWithSource(Object)`。
- **异常**：`isAddedPath(int)` 越界与 `isAddedPath(TreePath)` 未匹配路径抛 `IllegalArgumentException`。

## 2. Java 用户如何使用
`JTree` 派发，用户实现 `TreeSelectionListener`：

```java
tree.addTreeSelectionListener(e -> {
    TreePath[] paths = e.getPaths();
    for (int i = 0; i < paths.length; i++) {
        if (e.isAddedPath(i)) { /* 新增选中 paths[i] */ }
    }
    TreePath newLead = e.getNewLeadSelectionPath();
    TreePath first = e.getPath();
});
```

每个方法典型调用：`getPaths()` 遍历 → `isAddedPath(i)`/`isAddedPath(path)` 判断增删 → `getPath()` 取首个 → `getNewLeadSelectionPath()` 取导航锚点 → `cloneWithSource(newSource)` 转发事件给新源。

## 3. 开源代码实际用法调研
1. `test/jdk/javax/swing/event/bug4143690.java:38`：
   ```java
   TreeSelectionEvent e = new TreeSelectionEvent(test, p, true, p, p);
   TreePath[] paths = e.getPaths();
   if (e.isAddedPath(i) != true) throw new RuntimeException(...);
   ```
   直接构造并验证 `getPaths()/isAddedPath(int)`。
2. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTreeUI.java` 内部消费 `getPaths()/getNewLeadSelectionPath()` 驱动树 UI 刷新。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 EventObject、public、非 abstract、无接口）。
- 黑盒：数组构造器验证 `getPaths` 防御性拷贝、`getPath` 取首、三个 `isAddedPath` 重载、lead 路径 round-trip；单路径构造器验证单元素语义；`cloneWithSource` 验证新源 + 路径/标志/lead 完整拷贝。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
