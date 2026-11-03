# javax.swing.AbstractListModel 设计案

## 1. API 功能
`AbstractListModel<E>` 是 `ListModel<E>` 的抽象基类，管理 `ListDataListener` 列表。方法分组：
- 查询（抽象）：`getSize()`、`getElementAt(int)`。
- 监听：`addListDataListener`、`removeListDataListener`、`getListDataListeners`、`getListeners(Class<T>)`。
- 通知（受保护）：`fireContentsChanged`、`fireIntervalAdded`、`fireIntervalRemoved`。

## 2. Java 用户如何使用
用户继承它，实现 `getSize`/`getElementAt` 提供数据，再在数据变化时调用受保护的 `fire*` 方法通知视图刷新：
```java
class MyModel extends AbstractListModel<String> {
    public int getSize() { return data.size(); }
    public String getElementAt(int i) { return data.get(i); }
    void update() { fireContentsChanged(this, 0, getSize() - 1); }
}
```
典型链路：`new AbstractListModel(){...}` → `getSize/getElementAt` → `addListDataListener` → `fireContentsChanged` → 监听回调。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/DefaultListModel.java` 与 `javax/swing/DefaultComboBoxModel.java` 均 `extends AbstractListModel`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/`）。
- JDK 源码 `javax/swing/JList.java` 通过 `ListModel.addListDataListener` 订阅模型变更刷新视图。
- JDK 测试 `test/jdk/javax/swing/JList/` 大量使用 `DefaultListModel`（间接继承 `AbstractListModel`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract、实现 `ListModel`+`Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖匿名子类的 `getSize/getElementAt`、`add/remove/getListDataListeners`、受保护的 `fireContentsChanged` 触发 `contentsChanged` 回调、`getListeners(Class)` 类型过滤。
