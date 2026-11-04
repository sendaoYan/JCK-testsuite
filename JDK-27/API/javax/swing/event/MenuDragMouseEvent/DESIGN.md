# MenuDragMouseEvent 行为测试设计

## 1. API 功能
`javax.swing.event.MenuDragMouseEvent` 是菜单拖拽鼠标事件，继承 `java.awt.event.MouseEvent`。

- **构造器**：两组重载（9 参数与 11 参数），携带 source、id、when、modifiers、x/y、clickCount、popupTrigger，以及菜单路径 `MenuElement[]` 和 `MenuSelectionManager`。
- **查询**：`getPath()`（返回菜单路径数组，身份保留）、`getMenuSelectionManager()`，继承的 `getSource()/getID()/getWhen()/getX()/getY()/getClickCount()/isPopupTrigger()`。

## 2. Java 用户如何使用
`MenuItemUI` 内部在菜单项上拖拽时构造，用户实现 `MenuDragMouseListener`：

```java
menuItem.addMenuDragMouseListener(new MenuDragMouseListener() {
    public void menuDragMouseDragged(MenuDragMouseEvent e) {
        MenuElement[] path = e.getPath();          // 当前菜单路径
        MenuSelectionManager msm = e.getMenuSelectionManager();
        int x = e.getX(), y = e.getY();            // 相对菜单项的坐标
    }
    public void menuDragMouseEntered(MenuDragMouseEvent e) { }
    public void menuDragMouseExited(MenuDragMouseEvent e) { }
});
```

每个方法典型调用：`getSource()` 取得菜单项组件 → `getX()/getY()` 计算拖拽位置 → `getPath()`/`getMenuSelectionManager()` 处理菜单展开/高亮。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/JMenuItem.java:430`：
   ```java
   new MenuDragMouseEvent(e.getComponent(), e.getID(), ...);
   ```
   内部把 AWT MouseEvent 转换成 MenuDragMouseEvent。
2. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/SwingUtilities.java:379`：
   ```java
   newEvent = new MenuDragMouseEvent(newSource, ...);
   ```
   事件 retarget 时重建 MenuDragMouseEvent。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 MouseEvent、public、非 abstract、无接口）。
- 黑盒：用 `new Container()` 作源、`JMenuItem` 构造 `MenuElement[]` 路径、`new MenuSelectionManager()` 管理器（构造器为空，headless 安全），构造 9 参数事件验证 source/id/when/x/y/clickCount/popupTrigger/getPath/getMenuSelectionManager 的 round-trip。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe、不碰显示器。

## 5. 深化追加（第二轮）
- 构造器全重载补缺：9 参（无 modifiersEx 的 deprecated 形式）——source/id/when/modifiers/x/y/clickCount/popupTrigger/path/manager 全部 round-trip。
