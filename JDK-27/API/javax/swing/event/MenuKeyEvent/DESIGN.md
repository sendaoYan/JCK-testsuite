# MenuKeyEvent 行为测试设计

## 1. API 功能
`javax.swing.event.MenuKeyEvent` 是菜单键盘事件，继承 `java.awt.event.KeyEvent`。

- **构造器**：`MenuKeyEvent(Component source, int id, long when, int modifiers, int keyCode, char keyChar, MenuElement[] path, MenuSelectionManager manager)`。
- **查询**：`getPath()`（菜单路径数组）、`getMenuSelectionManager()`，继承的 `getSource()/getID()/getWhen()/getKeyCode()/getKeyChar()`。

## 2. Java 用户如何使用
`MenuItemUI` 内部处理键盘时构造，用户实现 `MenuKeyListener`：

```java
menuItem.addMenuKeyListener(new MenuKeyListener() {
    public void menuKeyTyped(MenuKeyEvent e) {
        char c = e.getKeyChar();                 // 键入字符
        MenuElement[] path = e.getPath();        // 当前菜单路径
    }
    public void menuKeyPressed(MenuKeyEvent e) { }
    public void menuKeyReleased(MenuKeyEvent e) { }
});
```

每个方法典型调用：`getKeyCode()/getKeyChar()` 判断按键 → `getPath()`/`getMenuSelectionManager()` 联动菜单选中状态。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/JMenuItem.java:457`：
   ```java
   MenuKeyEvent mke = new MenuKeyEvent(e.getComponent(), e.getID(), ...);
   ```
   把 AWT KeyEvent 转换成 MenuKeyEvent。
2. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/JPopupMenu.java:1429` 同样构造 `MenuKeyEvent` 处理弹出菜单键盘导航。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 KeyEvent、public、非 abstract、无接口）。
- 黑盒：用 `new Container()` 作源、`JMenuItem` 构造路径、`new MenuSelectionManager()`，构造 KEY_PRESSED 事件验证 source/id/when/keyCode/keyChar/getPath/getMenuSelectionManager 的 round-trip。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
