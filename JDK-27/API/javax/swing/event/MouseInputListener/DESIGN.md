# javax.swing.event.MouseInputListener 测试设计

## 1. API 功能

`MouseInputListener` 是合并鼠标监听器接口：`extends java.awt.event.MouseListener, java.awt.event.MouseMotionListener`（继承 `java.util.EventListener`），把点击类与移动类鼠标事件集中到一个接口，共 10 个回调：

- MouseListener：`mouseClicked / mousePressed / mouseReleased / mouseEntered / mouseExited(MouseEvent)`
- MouseMotionListener：`mouseDragged / mouseMoved(MouseEvent)`

（说明：`MouseListener` 本身 5 方法，`MouseMotionListener` 2 方法；任务所述的 10 方法即两个父接口方法全集。）

## 2. Java 用户如何使用

构造：实现接口（或用 `MouseInputAdapter` 省写）并注册：

```java
Component c = new Container();
MouseInputListener l = new MouseInputListener() {
    public void mouseClicked(MouseEvent e)  { /* 点击 */ }
    public void mousePressed(MouseEvent e)  { /* 按下 */ }
    public void mouseReleased(MouseEvent e) { /* 释放 */ }
    public void mouseEntered(MouseEvent e)  { /* 进入 */ }
    public void mouseExited(MouseEvent e)   { /* 离开 */ }
    public void mouseDragged(MouseEvent e)  { /* 拖拽 */ }
    public void mouseMoved(MouseEvent e)    { /* 移动 */ }
};
c.addMouseListener(l);
c.addMouseMotionListener(l);
```

典型组合链路：`addMouseListener + addMouseMotionListener → 进入 → mouseEntered → 按下 → mousePressed → 拖动 → mouseDragged → 释放 → mouseReleased → 点击 → mouseClicked`。事件 `MouseEvent` 携带 `getX/getY/getClickCount/getButton/getID`。

## 3. 开源代码实际用法调研

1. JDK `BasicTreeUI` 等 LAF 用 `MouseInputListener` 监听树/表头交互（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTreeUI.java`）。
2. `java.awt.TrayIcon` 也用到 `MouseInputListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/TrayIcon.java`），说明其跨 java.awt/javax.swing 使用。
3. `MouseInputListener` 接口源码（`src/java.desktop/share/classes/javax/swing/event/MouseInputListener.java`）声明 `extends MouseListener, MouseMotionListener`，是方法来源。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`MouseInputListenerTest.testBlackBox()`）：
- 匿名实现 7 个方法（MouseListener 5 + MouseMotionListener 2）并记录回调名与事件。
- 构造 `MouseEvent`（不同 id：`MOUSE_CLICKED/PRESSED/RELEASED/ENTERED/EXITED/DRAGGED/MOVED`，含坐标/点击数/按钮），逐一调用。
- 断言回调名正确、`getSource/getX/getY/getClickCount/getButton` round-trip。

白盒保留原有结构断言（接口、superclass null、两个父接口 `MouseListener`/`MouseMotionListener`），新增 7 个方法的 `getMethod` 存在性断言。
