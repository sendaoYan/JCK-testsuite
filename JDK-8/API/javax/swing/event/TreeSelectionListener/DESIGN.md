# javax.swing.event.TreeSelectionListener 测试设计

## 1. API 功能

`TreeSelectionListener` 监听树选择变化，继承 `java.util.EventListener`，一个回调：

- `void valueChanged(TreeSelectionEvent e)`：选择路径变化时触发。

事件 `TreeSelectionEvent` 携带：`getPath()`（首选路径）、`getPaths()`（全部路径）、`isAddedPath()`/`isAddedPath(TreePath)`/`isAddedPath(int)`（是否新增）、`getOldLeadSelectionPath()`/`getNewLeadSelectionPath()`、`cloneWithSource(Object)`。构造器有单路径与多路径两种。

## 2. Java 用户如何使用

构造：实现接口并注册到 `JTree`：

```java
JTree tree = new JTree();
TreeSelectionListener l = new TreeSelectionListener() {
    public void valueChanged(TreeSelectionEvent e) { /* 选择变化 */ }
};
tree.addTreeSelectionListener(l);
```

事件对象由 `JTree`/`DefaultTreeSelectionModel` 内部构造（`new TreeSelectionEvent(this, paths, areNew, oldLead, newLead)`）。

典型组合链路：`addTreeSelectionListener → setSelectionPath(path) → valueChanged → 读 getPath()/isAddedPath()/getNewLeadSelectionPath() → 追加多选 → getPaths() 含多条`。

## 3. 开源代码实际用法调研

1. JDK `JTree` 文档示例（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTree.java:137-138`）明确说明 `TreeSelectionListener` 的注册方式：
   ```java
   // ... the TreeSelectionListener interface and add the instance
   // using the method addTreeSelectionListener.
   ```
2. `JTree` 内部无障碍类实现 `valueChanged`，广播选择属性变化（同文件 `AccessibleJTree`）。
3. `DefaultTreeSelectionModel` 内部构造 `TreeSelectionEvent`（`src/java.desktop/share/classes/javax/swing/tree/DefaultTreeSelectionModel.java`），是 `getPaths/isAddedPath/getLeadSelectionPath` 的权威来源。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`TreeSelectionListenerTest.testBlackBox()`）：
- 匿名实现记录事件。
- 单路径构造器触发 `valueChanged`，断言 `getSource/getPath/isAddedPath/getOldLeadSelectionPath/getNewLeadSelectionPath` round-trip（含 `isAddedPath==false`）。
- 多路径构造器触发，断言 `getPaths()` 长度、`isAddedPath(int)`、`isAddedPath(TreePath)`。
- `cloneWithSource(newSource)` 断言新源与路径保留。

白盒保留原有结构断言，新增 `valueChanged(TreeSelectionEvent)` 返回 void/抽象与 `declared method count == 1`。
