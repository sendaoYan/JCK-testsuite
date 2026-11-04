# javax.swing.event.ListDataListener 测试设计

## 1. API 功能

`ListDataListener` 监听 `javax.swing.ListModel` 内容变化，继承 `java.util.EventListener`，三个回调：

- `void intervalAdded(ListDataEvent e)`：区间内元素被加入。
- `void intervalRemoved(ListDataEvent e)`：区间内元素被移除。
- `void contentsChanged(ListDataEvent e)`：区间内容被修改。

事件 `ListDataEvent` 携带 `getType()`（`CONTENTS_CHANGED/INTERVAL_ADDED/INTERVAL_REMOVED`）、`getIndex0()/getIndex1()`（受影响区间）与 `getSource()`。

## 2. Java 用户如何使用

构造：实现接口并注册到 `ListModel`（如 `DefaultListModel`）：

```java
DefaultListModel<String> model = new DefaultListModel<>();
ListDataListener l = new ListDataListener() {
    public void intervalAdded(ListDataEvent e)   { /* 新增 */ }
    public void intervalRemoved(ListDataEvent e) { /* 删除 */ }
    public void contentsChanged(ListDataEvent e) { /* 修改 */ }
};
model.addListDataListener(l);
model.addElement("a");            // intervalAdded，index0==index1==0
model.removeElementAt(0);         // intervalRemoved
```

典型组合链路：`addListDataListener → addElement → intervalAdded（读 getIndex0/getIndex1）→ setElementAt → contentsChanged → removeElementAt → intervalRemoved`。

## 3. 开源代码实际用法调研

1. JDK `JComboBox` 直接实现 `ListDataListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JComboBox.java:114,354`）：
   ```java
   public class JComboBox ... implements ItemSelectable, ListDataListener, ActionListener, Accessible ...
   ...
   dataModel.addListDataListener(this);
   ```
2. `AbstractListModel` 提供 `addListDataListener/fireIntervalAdded/fireIntervalRemoved/fireContentsChanged`，内部构造 `ListDataEvent`（`src/java.desktop/share/classes/javax/swing/AbstractListModel.java`）。
3. `DefaultListModel` 的 `addElement/removeElement/setElementAt` 分别触发三种事件，是 `ListDataEvent` 三种 type 的真实来源。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`ListDataListenerTest.testBlackBox()`）：
- 匿名实现记录最近回调名与事件。
- 构造三个 `ListDataEvent`（三种 type + 不同 index 区间），分别调用三个回调。
- 断言回调名正确、事件实例一致、`getType()/getIndex0()/getIndex1()/getSource()` 全部 round-trip；三个 type 常量互异。

白盒保留原有结构断言，新增三方法签名/抽象性与 `declared method count == 3`。
