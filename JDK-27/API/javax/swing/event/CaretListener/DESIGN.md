# javax.swing.event.CaretListener 测试设计

## 1. API 功能

`CaretListener` 监听文本组件插入符（光标）位置变化，继承 `java.util.EventListener`，只有一个回调：

- `void caretUpdate(CaretEvent e)`：插入符位置（dot/mark）变化时触发。

配套事件 `CaretEvent` 是抽象类（继承 `java.util.EventObject`），定义 `abstract int getDot()`（当前位置）与 `abstract int getMark()`（选区另一端点）。实际事件由 `javax.swing.text.JTextComponent` 内部（`DefaultCaret`）以匿名/内部子类形式构造。

## 2. Java 用户如何使用

构造：用户实现接口并注册到文本组件：

```java
JTextComponent c = new JTextArea();
CaretListener l = new CaretListener() {
    public void caretUpdate(CaretEvent e) { /* 光标移动 */ }
};
c.addCaretListener(l);   // JTextComponent.addCaretListener
```

事件对象通常由 Swing 内部构造；测试时可用匿名 `CaretEvent` 子类提供 `getDot()/getMark()` 值。

典型组合链路：`new JTextArea → addCaretListener → setCaretPosition(5) → caretUpdate 回调 → e.getDot() 返回 5`。`getDot()` 与 `getMark()` 分别反映光标与选区端点，无选区时二者相等。

## 3. 开源代码实际用法调研

1. JDK 实现 `JTextComponent` 提供注册入口（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/JTextComponent.java:356`）：
   ```java
   public void addCaretListener(CaretListener listener) {
       listenerList.add(CaretListener.class, listener);
   }
   ```
2. `JTextComponent.AccessibleJTextComponent` 实现 `CaretListener`（同文件 `:2549`），用于无障碍（Accessibility）跟踪光标变化：
   ```java
   ... implements AccessibleText, CaretListener, DocumentListener ...
   ```
3. `DefaultCaret`（`javax/swing/text/DefaultCaret.java`）在 `setDot`/`moveDot` 时 `fireStateChanged()`，进而构造 `CaretEvent` 通知所有 `CaretListener`。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`CaretListenerTest.testBlackBox()`）：
- 匿名 `CaretListener` 记录收到的 `CaretEvent`。
- 用匿名 `CaretEvent` 子类实现 `getDot()=3`、`getMark()=5`，`getSource()` 为独立对象。
- 调用 `caretUpdate`，断言回调被触发、事件实例一致、`getSource()/getDot()/getMark()` 全部 round-trip，且 dot 与 mark 相互独立。

白盒部分保留原有结构断言，并新增 `caretUpdate(CaretEvent)` 返回 void/抽象与 `declared method count == 1`。
