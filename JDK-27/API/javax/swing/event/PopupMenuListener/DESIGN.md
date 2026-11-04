# javax.swing.event.PopupMenuListener 测试设计

## 1. API 功能

`PopupMenuListener` 监听弹出菜单（`JPopupMenu`/组合框下拉）的显示/隐藏生命周期，继承 `java.util.EventListener`，三个回调：

- `popupMenuWillBecomeVisible(PopupMenuEvent e)`：弹出前。
- `popupMenuWillBecomeInvisible(PopupMenuEvent e)`：收起前。
- `popupMenuCanceled(PopupMenuEvent e)`：弹出被取消（无选择）。

事件 `PopupMenuEvent`（继承 `java.util.EventObject`，构造 `PopupMenuEvent(Object source)`）只携带事件源。

## 2. Java 用户如何使用

构造：实现接口并注册到 `JPopupMenu`：

```java
JPopupMenu popup = new JPopupMenu();
PopupMenuListener l = new PopupMenuListener() {
    public void popupMenuWillBecomeVisible(PopupMenuEvent e)  { /* 弹出前 */ }
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { /* 收起前 */ }
    public void popupMenuCanceled(PopupMenuEvent e)           { /* 取消 */ }
};
popup.addPopupMenuListener(l);
```

事件对象由 `JPopupMenu` 内部构造 `new PopupMenuEvent(this)` 分发。

典型组合链路：`addPopupMenuListener → 触发弹出 → popupMenuWillBecomeVisible → 选择/收起 → popupMenuWillBecomeInvisible → 移出取消 → popupMenuCanceled`。

## 3. 开源代码实际用法调研

1. JDK `JPopupMenu.addPopupMenuListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JPopupMenu.java:625`）：
   ```java
   public void addPopupMenuListener(PopupMenuListener l) {
       listenerList.add(PopupMenuListener.class, l);
   }
   ```
2. `JPopupMenu.getPopupMenuListeners()`（同文件 `:642` 附近）返回注册数组。
3. `JPopupMenu` 在 `firePopupMenuWillBecomeVisible/firePopupMenuWillBecomeInvisible/firePopupMenuCanceled` 内部 `new PopupMenuEvent(this)` 后派发，是 `PopupMenuEvent` 构造来源。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`PopupMenuListenerTest.testBlackBox()`）：
- 匿名实现记录最近回调名与事件。
- 构造三个 `PopupMenuEvent`（共享 source），分别调用三个回调。
- 断言回调名正确、事件实例一致、`getSource()` round-trip。

白盒保留原有结构断言，新增三方法签名/抽象性与 `declared method count == 3`。
