# AWTEventMonitor 设计案

## 1. API 功能
`com.sun.java.accessibility.util.AWTEventMonitor` 是一个「静态监听器注册器 + 事件监视器」：把每个 AWT 组件上分散的事件汇总成按类别划分的一组监听器，并维护当前拥有键盘焦点的组件。

- 构造器：`public AWTEventMonitor()`。
- 查询：`public static Component getComponentWithFocus()`。
- 注册/注销（全部 `public static`）：`add/removeComponentListener`、`add/removeContainerListener`、`add/removeFocusListener`、`add/removeKeyListener`、`add/removeMouseListener`、`add/removeMouseMotionListener`、`add/removeWindowListener`、`add/removeActionListener`、`add/removeAdjustmentListener`、`add/removeItemListener`、`add/removeTextListener`。

## 2. Java 用户如何使用
用户不实例化（也允许实例化），直接调用静态方法按类别注册监听器；当任意组件产生对应事件时，内部 `AWTEventsListener` 会把事件转发给已注册的监听器；`getComponentWithFocus()` 返回当前聚焦组件（无则为 null）。

组合链路（来自 JDK 内部）：
```
AWTEventMonitor.addFocusListener(myFocusListener);
AWTEventMonitor.addMouseListener(myMouseListener);
Component c = AWTEventMonitor.getComponentWithFocus();
AWTEventMonitor.removeFocusListener(myFocusListener);
```

## 3. 开源代码实际用法调研
- Windows 辅助功能桥 `~/git/jdk25u-dev/src/jdk.accessibility/windows/classes/com/sun/java/accessibility/internal/AccessBridge.java` 第 651 行使用 `AWTEventMonitor.getComponentWithFocus()`。
- JDK 源码内部 `AWTEventMonitor.java` 自身组合使用 `EventQueueMonitor.getTopLevelWindows()`、`EventID.*`、`AWTEventMulticaster` 与 `MenuSelectionManager`；`getComponentWithFocus()` 直接返回静态字段 `componentWithFocus`。
- 行为权威：`~/git/jdk25u-dev/src/jdk.accessibility/share/classes/com/sun/java/accessibility/util/AWTEventMonitor.java`。
- 注：`~/git/jdk25u-dev/test/jdk` 无本类直接功能用例。

## 4. 测试用例设计 + 自动化实现
- 白盒：类非接口、为具体类、有公有构造器；11 类监听器方法名存在（保留旧断言并补齐）。
- 黑盒（`testBlackBox()`）：headless 下（不 setVisible 任何组件、无窗口）：
  - `new AWTEventMonitor()` 不抛；`getComponentWithFocus()` 返回 null。
  - 逐个对 11 类监听器执行「匿名实现 add → 校验 focus 仍 null → remove」，全程不抛异常、不触碰 DISPLAY。
  - 组合场景：同一类别链式 add 两个监听器再逐个 remove，校验无残留、focus 仍 null。
  - 结论：所有 add/remove 在 headless 下均安全（内部 `installListeners` 对空顶层窗口列表为 no-op）。
