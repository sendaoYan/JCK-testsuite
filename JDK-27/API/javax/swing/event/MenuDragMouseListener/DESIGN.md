# javax.swing.event.MenuDragMouseListener 测试设计

## 1. API 功能

`MenuDragMouseListener` 监听菜单项上拖拽鼠标事件，继承 `java.util.EventListener`，四个回调：

- `menuDragMouseEntered(MenuDragMouseEvent e)`：拖拽进入菜单项。
- `menuDragMouseExited(MenuDragMouseEvent e)`：拖拽离开菜单项。
- `menuDragMouseDragged(MenuDragMouseEvent e)`：在菜单项上拖拽。
- `menuDragMouseReleased(MenuDragMouseEvent e)`：在菜单项上释放。

事件 `MenuDragMouseEvent`（继承 `MouseEvent`）额外携带 `getPath()`（`MenuElement[]` 菜单路径）与 `getMenuSelectionManager()`，并继承 `getX/getY/getClickCount/isPopupTrigger/getID`。

## 2. Java 用户如何使用

构造：实现接口并注册到 `JMenuItem`：

```java
JMenuItem item = new JMenuItem("open");
MenuDragMouseListener l = new MenuDragMouseListener() {
    public void menuDragMouseEntered(MenuDragMouseEvent e)  { /* 进入 */ }
    public void menuDragMouseExited(MenuDragMouseEvent e)   { /* 离开 */ }
    public void menuDragMouseDragged(MenuDragMouseEvent e)  { /* 拖拽 */ }
    public void menuDragMouseReleased(MenuDragMouseEvent e) { /* 释放 */ }
};
item.addMenuDragMouseListener(l);
```

事件对象由 `JMenuItem` 在 `processMenuDragMouseEvent` 路径构造（带 `MenuElement[]` 与 `MenuSelectionManager`）。测试时可用 10 参构造器 `new MenuDragMouseEvent(component, id, when, modifiers, x, y, clickCount, popupTrigger, path, msm)`。

典型组合链路：`addMenuDragMouseListener → 拖入菜单项 → menuDragMouseEntered → 拖动 → menuDragMouseDragged → 释放 → menuDragMouseReleased → 读 e.getPath() 得到当前菜单路径`。

## 3. 开源代码实际用法调研

1. JDK `JMenuItem.addMenuDragMouseListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JMenuItem.java:705`）注册监听器；`processMenuDragMouseEvent`（`:475`）把 `MenuDragMouseEvent` 派发给注册的监听器。
2. `JMenuItem` 的 `processMouseEvent`（`:429` 附近）在拖拽时 `new MenuDragMouseEvent(...)` 后调用 `processMenuDragMouseEvent`，是事件的真实构造来源。
3. `MenuDragMouseEvent` 10 参构造器（`src/java.desktop/share/classes/javax/swing/event/MenuDragMouseEvent.java:79`）把 `path`/`manager` 存入字段，`getPath()/getMenuSelectionManager()` 直接返回，是本测试 round-trip 断言的依据。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`MenuDragMouseListenerTest.testBlackBox()`）：
- 匿名实现记录最近回调名与事件。
- 构造 `MenuDragMouseEvent`（source=`Container`，path 含一个 `JMenuItem`，`MenuSelectionManager` 独立实例），分别以 `MOUSE_ENTERED/EXITED/DRAGGED/RELEASED` 触发四个回调。
- 断言回调名正确、`getSource/getPath/getMenuSelectionManager/getX/getY/getClickCount/isPopupTrigger/getID` 全部 round-trip。

白盒保留原有结构断言，新增四方法签名/抽象性与 `declared method count == 4`。
