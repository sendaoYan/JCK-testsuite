# javax.swing.event.ChangeListener 测试设计

## 1. API 功能

`ChangeListener` 是 Swing 中最通用的「状态变化」监听器，继承 `java.util.EventListener`，仅一个回调：

- `void stateChanged(ChangeEvent e)`：被监听对象状态改变时触发。

事件 `ChangeEvent` 继承 `java.util.EventObject`，构造 `ChangeEvent(Object source)`，只携带事件源，不携带额外状态。大量 Swing 组件（按钮模型、滑块、微调器、选择模型等）都用它广播「我变了」。

## 2. Java 用户如何使用

构造：实现接口并注册到任意支持 `ChangeListener` 的模型/组件：

```java
BoundedRangeModel model = new DefaultBoundedRangeModel(0, 0, 0, 100);
ChangeListener l = new ChangeListener() {
    public void stateChanged(ChangeEvent e) { /* 值变了 */ }
};
model.addChangeListener(l);
model.setValue(50);   // 触发 stateChanged(new ChangeEvent(model))
```

典型组合链路：`new DefaultBoundedRangeModel → addChangeListener → setValue(50) → stateChanged 回调 → e.getSource() 返回该 model`。因为事件只有 source，用户通常在回调里再次读取模型当前值。

## 3. 开源代码实际用法调研

1. JDK 实现 `JTable` 等大量类内部注册/实现 `ChangeListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTable.java` 等）；`JSlider`/`JSpinner` 依赖它监听模型变化。
2. `SpinnerListModel`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/SpinnerListModel.java`）用 `ChangeEvent` 通知值变化：
   ```java
   fireStateChanged();  // 内部构造 new ChangeEvent(this) 分发给 ChangeListener
   ```
3. `test/jdk/javax/swing/JSpinner/` 多个回归测试（如 `4973721/bug4973721.java`、`4788637/bug4788637.java`）通过 `addChangeListener` 断言微调器值变化被通知。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`ChangeListenerTest.testBlackBox()`）：
- 匿名实现记录收到的 `ChangeEvent`。
- 构造 `ChangeEvent(source)`，调用 `stateChanged`，断言回调触发、事件实例一致、`getSource()` round-trip。
- 再次调用第二个事件，验证监听器可重复/可重入触发。

白盒保留原有结构断言，新增 `stateChanged(ChangeEvent)` 返回 void/抽象与 `declared method count == 1`。
