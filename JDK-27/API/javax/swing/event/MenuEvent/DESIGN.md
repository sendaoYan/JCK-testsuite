# MenuEvent 行为测试设计

## 1. API 功能
`javax.swing.event.MenuEvent` 是菜单选中/取消/取消中的事件，继承 `java.util.EventObject`。

- **构造器**：`MenuEvent(Object source)`。
- **查询**：继承的 `getSource()`、`toString()`（EventObject 实现内嵌 source 的 `toString()`）。

## 2. Java 用户如何使用
`JMenu` 派发，用户实现 `MenuListener`：

```java
menu.addMenuListener(new MenuListener() {
    public void menuSelected(MenuEvent e) {
        JMenu m = (JMenu) e.getSource();   // 取得被选中的菜单
    }
    public void menuDeselected(MenuEvent e) { }
    public void menuCanceled(MenuEvent e) { }
});
```

每个方法典型调用：`new MenuEvent(menu)` 构造 → `getSource()` 回读菜单 → 向下转型后调用菜单方法（如 `getItemCount()`）。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/JMenu.java` 内部 `fireMenuSelected/fireMenuDeselected/fireMenuCanceled` 构造 `new MenuEvent(this)` 派发。
2. `test/jdk` 中 `MenuListener` 用例（如 `javax/swing/JMenu` 相关回归）读取 `e.getSource()` 验证菜单状态。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 EventObject、public、非 abstract、无接口）。
- 黑盒：`new MenuEvent(source)` 断言 `getSource()` 身份；用 `JMenu` 作源断言 round-trip 与 `toString()` 内嵌源文本，形成构造 → 读取 → 字符串化组合链。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
