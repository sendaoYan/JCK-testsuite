# javax.swing.event.AncestorListener 测试设计

## 1. API 功能

`AncestorListener` 是一个标记 Swing 组件「祖先层级变化」的监听器接口，继承 `java.util.EventListener`。当注册监听器的组件（`JComponent`）在组件树中被加入/移出可见祖先，或其祖先位置变化时收到回调。它本身无构造器（接口），全部能力是三个回调方法：

- `void ancestorAdded(AncestorEvent e)`：祖先被加入可见层级时触发。
- `void ancestorRemoved(AncestorEvent e)`：祖先被移出可见层级时触发。
- `void ancestorMoved(AncestorEvent e)`：祖先位置变化时触发。

配套事件 `AncestorEvent`（继承 `java.awt.AWTEvent`）携带：`getComponent()`（事件源 `JComponent`）、`getAncestor()`（发生变化的 `Container`）、`getAncestorParent()`（祖先的父 `Container`）、`getID()`（`ANCESTOR_ADDED=1` / `ANCESTOR_REMOVED=2` / `ANCESTOR_MOVED=3`）。

## 2. Java 用户如何使用

构造：用户不直接构造监听器，而是实现接口并注册：

```java
JComponent comp = new JPanel();
AncestorListener l = new AncestorListener() {
    public void ancestorAdded(AncestorEvent e)   { /* 显示时 */ }
    public void ancestorRemoved(AncestorEvent e) { /* 隐藏时 */ }
    public void ancestorMoved(AncestorEvent e)   { /* 移动时 */ }
};
comp.addAncestorListener(l);   // JComponent.addAncestorListener
```

事件对象由 Swing 内部构造（`JComponent.AncestorNotifier`），但测试时用户也可手工构造：`new AncestorEvent(component, id, ancestor, ancestorParent)`。

典型组合链路：`new JComponent → addAncestorListener(listener) → 组件加入父容器 → ancestorAdded 被回调 → 读 e.getAncestor()/getAncestorParent() 确认层级`。三个方法各对应一种层级事件；读取 `getID()` 区分三种事件类型。

## 3. 开源代码实际用法调研

1. JDK 实现 `JComponent` 提供注册入口（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JComponent.java:4731`）：
   ```java
   public void addAncestorListener(AncestorListener listener) {
       ...
       ancestorNotifier.addAncestorListener(listener);
   }
   ```
2. `JComponent.AncestorNotifier` 内部在组件层级变化时构造 `AncestorEvent` 并分发（同文件 `AncestorNotifier` 内 `fireAncestorAdded/fireAncestorRemoved/fireAncestorMoved`），是 `AncestorEvent` 三个 id 常量的真实来源。
3. `test/jdk/javax/swing/` 下大量用例通过 `addAncestorListener` 监听组件显示/隐藏时机，例如 `JComponent` 相关回归测试用 `ancestorAdded` 判断组件已可显示。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`AncestorListenerTest.testBlackBox()`）：
- 匿名实现三个回调，用 `String[]` 记录最近一次回调名、`AncestorEvent[]` 记录事件对象。
- 手工构造 `AncestorEvent`（source=`JPanel`，ancestor/ancestorParent 各为独立 `Container`），分别调用 `ancestorAdded/ancestorRemoved/ancestorMoved`。
- 断言：每次回调被触发、传入的事件实例一致、`getComponent()/getAncestor()/getAncestorParent()/getID()` 全部 round-trip 到构造入参；三个 id 常量稳定且互异。

白盒部分保留原有结构断言，并新增三个方法的签名/返回类型/抽象性断言与 `declared method count == 3`。用例 headless 安全（仅构造轻量 `JPanel`/`Container`，不实例化顶层窗口）。
