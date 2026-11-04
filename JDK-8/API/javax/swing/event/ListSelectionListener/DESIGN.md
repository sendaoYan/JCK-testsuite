# javax.swing.event.ListSelectionListener 测试设计

## 1. API 功能

`ListSelectionListener` 监听列表/表格选择区间变化，继承 `java.util.EventListener`，一个回调：

- `void valueChanged(ListSelectionEvent e)`：选择区间变化时触发。

事件 `ListSelectionEvent` 携带 `getFirstIndex()/getLastIndex()`（受影响的闭区间）、`getValueIsAdjusting()`（是否处于连续拖动选择中）与 `getSource()`。

## 2. Java 用户如何使用

构造：实现接口并注册到 `ListSelectionModel`（或 `JList/JTable` 的选择模型）：

```java
ListSelectionModel sm = new DefaultListSelectionModel();
ListSelectionListener l = new ListSelectionListener() {
    public void valueChanged(ListSelectionEvent e) { /* 选择变化 */ }
};
sm.addListSelectionListener(l);
sm.setSelectionInterval(2, 4);   // valueChanged，first=2 last=4
```

典型组合链路：`addListSelectionListener → setSelectionInterval(2,4) → valueChanged → 读 getFirstIndex/getLastIndex/getValueIsAdjusting → 拖动时 getValueIsAdjusting()==true，松开后==false`。

## 3. 开源代码实际用法调研

1. JDK `JTable` 声明实现 `ListSelectionListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTable.java:281`），`valueChanged`（`:4765`）处理行列选择联动：
   ```java
   public void valueChanged(ListSelectionEvent e) {
       ...
       boolean isAdjusting = e.getValueIsAdjusting();
       ...
   }
   ```
2. `JTable` 内部无障碍类也实现 `valueChanged`（同文件 `:7121`）广播选择变化属性。
3. `ListSelectionModel` 接口文档/`DefaultListSelectionModel` 内部构造 `ListSelectionEvent`，是 `getFirstIndex/getLastIndex/getValueIsAdjusting` 的权威来源。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`ListSelectionListenerTest.testBlackBox()`）：
- 匿名实现记录事件。
- 构造两个 `ListSelectionEvent`（调整中 true / false、单点与区间），调用 `valueChanged`。
- 断言回调触发、`getSource/getFirstIndex/getLastIndex/getValueIsAdjusting` 全部 round-trip，含单选区间与 `getValueIsAdjusting` 两种取值。

白盒保留原有结构断言，新增 `valueChanged(ListSelectionEvent)` 返回 void/抽象与 `declared method count == 1`。
