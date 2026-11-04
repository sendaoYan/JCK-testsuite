# javax.swing.event.MouseInputAdapter 测试设计

## 1. API 功能

`MouseInputAdapter` 是鼠标事件便捷适配器：继承 `java.awt.event.MouseAdapter` 并实现 `javax.swing.event.MouseInputListener`（即 `MouseListener + MouseMotionListener` 的合并），构造器 `protected MouseInputAdapter()`。它把鼠标相关回调全部实现为空操作（no-op），让用户只重写关心的方法。可继承的方法（来自 `MouseAdapter`）：

- `mouseClicked / mousePressed / mouseReleased / mouseEntered / mouseExited(MouseEvent)`
- `mouseWheelMoved(MouseWheelEvent)`
- `mouseDragged / mouseMoved(MouseEvent)`

## 2. Java 用户如何使用

构造：用户匿名子类化并只重写需要的方法：

```java
Component c = new Container();
MouseInputAdapter a = new MouseInputAdapter() {
    public void mouseClicked(MouseEvent e) { /* 点击 */ }
    public void mouseDragged(MouseEvent e) { /* 拖拽 */ }
};
c.addMouseListener(a);
c.addMouseMotionListener(a);
```

典型组合链路：`addMouseListener + addMouseMotionListener 同一个适配器 → 点击 → mouseClicked → 拖动 → mouseDragged → 移动 → mouseMoved`。未重写方法为 no-op，调用不抛异常。

## 3. 开源代码实际用法调研

1. JDK `BasicInternalFrameUI.BorderListener` 子类化 `MouseInputAdapter` 处理内部窗口边框拖拽/调整（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicInternalFrameUI.java:838`）：
   ```java
   protected class BorderListener extends MouseInputAdapter implements SwingConstants { ... }
   ```
2. `BasicSliderUI` 等多处 LAF 类用 `MouseInputAdapter` 监听滑块/表头拖拽（`src/java.desktop/share/classes/javax/swing/plaf/basic/BasicSliderUI.java`）。
3. `MouseInputListener` 接口源码（`src/java.desktop/share/classes/javax/swing/event/MouseInputListener.java`）即 `extends MouseListener, MouseMotionListener`，说明适配器覆盖的两个接口来源。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`MouseInputAdapterTest.testBlackBox()`）：
- 匿名子类重写 8 个方法（含 `mouseWheelMoved`）并记录回调名，构造 `MouseEvent`/`MouseWheelEvent`，逐一调用 8 个方法，断言各自被重写回调。
- 再创建普通子类（不重写），调用 7 个鼠标 no-op 方法，验证不抛异常。
- 断言适配器实例 `instanceof MouseInputListener`。

白盒保留原有结构断言（superclass `MouseAdapter`、abstract、接口 `MouseInputListener`），新增构造器 `protected`。
