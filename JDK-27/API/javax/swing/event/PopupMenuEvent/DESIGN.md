# PopupMenuEvent 行为测试设计

## 1. API 功能
`javax.swing.event.PopupMenuEvent` 是弹出菜单可见性变化事件，继承 `java.util.EventObject`。

- **构造器**：`PopupMenuEvent(Object source)`。
- **查询**：继承的 `getSource()`、`toString()`。

## 2. Java 用户如何使用
`JPopupMenu` 派发，用户实现 `PopupMenuListener`：

```java
popup.addPopupMenuListener(new PopupMenuListener() {
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        JPopupMenu p = (JPopupMenu) e.getSource();
    }
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }
    public void popupMenuCanceled(PopupMenuEvent e) { }
});
```

每个方法典型调用：`new PopupMenuEvent(popup)` 构造 → `getSource()` 回读弹出菜单 → 调用菜单方法。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/JPopupMenu.java` 内部 `firePopupMenuWillBecomeVisible/.../firePopupMenuCanceled` 构造 `new PopupMenuEvent(this)` 派发。
2. `test/jdk` 中 `PopupMenuListener` 回归用例读取 `e.getSource()` 验证弹出菜单状态。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 EventObject、public、非 abstract、无接口）。
- 黑盒：`new PopupMenuEvent(source)` 断言 `getSource()` 身份；用 `JPopupMenu` 作源断言 round-trip 与 `toString()` 内嵌源文本。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
