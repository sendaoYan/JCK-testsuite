# javax.swing.event.MenuListener 测试设计

## 1. API 功能

`MenuListener` 监听菜单的选中/取消/关闭生命周期，继承 `java.util.EventListener`，三个回调：

- `menuSelected(MenuEvent e)`：菜单被选中（弹出）。
- `menuDeselected(MenuEvent e)`：菜单被取消选中。
- `menuCanceled(MenuEvent e)`：菜单被关闭（未选择任何项）。

事件 `MenuEvent`（继承 `java.util.EventObject`，构造 `MenuEvent(Object source)`）只携带事件源（`JMenu`）。

## 2. Java 用户如何使用

构造：实现接口并注册到 `JMenu`：

```java
JMenu menu = new JMenu("File");
MenuListener l = new MenuListener() {
    public void menuSelected(MenuEvent e)  { /* 弹出 */ }
    public void menuDeselected(MenuEvent e) { /* 收起 */ }
    public void menuCanceled(MenuEvent e)  { /* 关闭 */ }
};
menu.addMenuListener(l);
```

事件对象由 `JMenu` 内部构造 `new MenuEvent(this)` 分发。

典型组合链路：`addMenuListener → 点菜单 → menuSelected → 移开/选择子项 → menuDeselected → 按 Esc/移出 → menuCanceled`。三方法覆盖「弹出→选中→取消」的菜单交互时序。

## 3. 开源代码实际用法调研

1. JDK `JMenu.addMenuListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JMenu.java:1013`）：
   ```java
   public void addMenuListener(MenuListener l) {
       listenerList.add(MenuListener.class, l);
   }
   ```
2. `JMenu.getMenuListeners()`（同文件 `:1035`）返回注册的 `MenuListener[]`。
3. `JMenu` 在 `fireMenuSelected/fireMenuDeselected/fireMenuCanceled` 内部 `new MenuEvent(this)` 后派发，是 `MenuEvent` 的构造来源（同文件）。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`MenuListenerTest.testBlackBox()`）：
- 匿名实现记录最近回调名与事件。
- 构造三个 `MenuEvent`（共享 source），分别调用 `menuSelected/menuDeselected/menuCanceled`。
- 断言回调名正确、事件实例一致、`getSource()` round-trip。

白盒保留原有结构断言，新增三方法签名/抽象性与 `declared method count == 3`。
