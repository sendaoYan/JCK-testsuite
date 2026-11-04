# javax.swing.event.MenuKeyListener 测试设计

## 1. API 功能

`MenuKeyListener` 监听菜单项上的键盘事件，继承 `java.util.EventListener`，三个回调：

- `menuKeyTyped(MenuKeyEvent e)`：键被敲击（字符产生）。
- `menuKeyPressed(MenuKeyEvent e)`：键被按下。
- `menuKeyReleased(MenuKeyEvent e)`：键被释放。

事件 `MenuKeyEvent`（继承 `KeyEvent`）额外携带 `getPath()`（`MenuElement[]`）与 `getMenuSelectionManager()`，并继承 `getKeyCode/getKeyChar/getID`。

## 2. Java 用户如何使用

构造：实现接口并注册到 `JMenuItem`：

```java
JMenuItem item = new JMenuItem("open");
MenuKeyListener l = new MenuKeyListener() {
    public void menuKeyTyped(MenuKeyEvent e)    { /* typed */ }
    public void menuKeyPressed(MenuKeyEvent e)  { /* pressed */ }
    public void menuKeyReleased(MenuKeyEvent e) { /* released */ }
};
item.addMenuKeyListener(l);
```

事件对象由 `JMenuItem.processMenuKeyEvent` 路径构造（`MenuKeyEvent(component, id, when, modifiers, keyCode, keyChar, path, msm)`）。测试时可直接手工构造。

典型组合链路：`addMenuKeyListener → 按下键 → menuKeyPressed（读 getKeyCode）→ 释放 → menuKeyReleased → 产生字符 → menuKeyTyped（读 getKeyChar）`。

## 3. 开源代码实际用法调研

1. JDK `JMenuItem.addMenuKeyListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JMenuItem.java:736`）注册监听器；`processMenuKeyEvent`（`:495`）派发 `MenuKeyEvent`。
2. `JMenuItem.processKeyEvent`（`:461` 附近）在菜单键盘处理时 `new MenuKeyEvent(...)` 后调用 `processMenuKeyEvent`，是事件构造来源。
3. `MenuKeyEvent` 构造器（`src/java.desktop/share/classes/javax/swing/event/MenuKeyEvent.java:72`）把 `path`/`manager` 存入字段，`getPath()/getMenuSelectionManager()` 直接返回，是本测试 round-trip 依据。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`MenuKeyListenerTest.testBlackBox()`）：
- 匿名实现记录最近回调名与事件。
- 构造三个 `MenuKeyEvent`（`KEY_TYPED` 带 `'a'`，`KEY_PRESSED/KEY_RELEASED` 带 `VK_ENTER`），分别触发三个回调。
- 断言回调名正确、`getSource/getPath/getMenuSelectionManager/getKeyChar/getKeyCode/getID` 全部 round-trip。

白盒保留原有结构断言，新增三方法签名/抽象性与 `declared method count == 3`。
