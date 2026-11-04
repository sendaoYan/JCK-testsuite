# CaretEvent 行为测试设计

## 1. API 功能
`javax.swing.event.CaretEvent` 是文本光标事件，抽象类，继承 `java.util.EventObject`。

- **构造器**：`CaretEvent(Object source)`（protected/public 供子类调用）。
- **抽象查询**：`getDot()`（光标插入点/选中端之一）、`getMark()`（另一端），二者组成选择区间。

## 2. Java 用户如何使用
用户不直接 new，而是由 `JTextComponent` 内部子类（如 `MutableCaretEvent`）实例化，用户实现 `CaretListener` 读取：

```java
textComponent.addCaretListener(e -> {
    int dot = e.getDot();      // 光标当前位置
    int mark = e.getMark();    // 选择锚点
    int selStart = Math.min(dot, mark);
    int selEnd = Math.max(dot, mark);
});
```

每个方法典型调用：`getSource()` 取得文本组件源、`getDot()`/`getMark()` 组合计算选择范围（`min/max` 得到起点/终点）。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/JTextComponent.java:313`：
   ```java
   caretEvent = new MutableCaretEvent(this);
   ```
   内部在 caret 状态变化时构造 `CaretEvent` 子类并 `fireCaretUpdate`。
2. `test/jdk` 中 `CaretListener` 回调读取 `e.getDot()`/`e.getMark()` 验证光标位置的用例广泛存在。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 EventObject、public、abstract、无接口）。
- 黑盒：用两个匿名子类覆盖 `getDot/getMark`，验证构造器传入的 source 与 `getSource()`、以及 dot/mark 的 round-trip；第二个实例用不同值证明无静态共享状态；覆盖负值/零值边界。
- 自动化：`testWhiteBox()` + `testBlackBox()`，多步组合（构造 → `getSource` + `getDot` + `getMark` 相互印证），headless-safe。
