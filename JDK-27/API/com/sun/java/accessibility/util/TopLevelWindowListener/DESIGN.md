# TopLevelWindowListener 设计案

## 1. API 功能
`com.sun.java.accessibility.util.TopLevelWindowListener` 是一个回调接口（`extends java.util.EventListener`），监听顶层窗口的创建与销毁。

- 抽象方法：`void topLevelWindowCreated(Window w)`、`void topLevelWindowDestroyed(Window w)`。
- 无构造器（接口）。

## 2. Java 用户如何使用
实现该接口并注册到 `EventQueueMonitor.addTopLevelWindowListener(...)`；当 `EventQueueMonitor` 缓存到新的顶层 `Window` 或窗口被销毁时回调。

组合链路（来自 JDK 内部 `EventQueueMonitor`）：
```
EventQueueMonitor.addTopLevelWindowListener(myListener);   // 注册
// 窗口出现/销毁时：
myListener.topLevelWindowCreated(window);
myListener.topLevelWindowDestroyed(window);
```

## 3. 开源代码实际用法调研
- JDK 源码内部：`~/git/jdk25u-dev/src/jdk.accessibility/share/classes/com/sun/java/accessibility/util/EventQueueMonitor.java` 在 `addTopLevelWindow` / `removeTopLevelWindow` 中调用 `topLevelWindowListener.topLevelWindowCreated((Window) parent)` / `topLevelWindowListener.topLevelWindowDestroyed(w)`。
- JDK 源码内部：`AWTEventMonitor$AWTEventsListener`、`AccessibilityEventMonitor$AccessibilityEventListener`、`SwingEventMonitor$SwingEventListener` 都实现本接口，用于在新窗口出现时自动安装监听器。
- 行为权威：`~/git/jdk25u-dev/src/jdk.accessibility/share/classes/com/sun/java/accessibility/util/TopLevelWindowListener.java`。
- 注：本接口有两个抽象方法，非函数式接口；`test/jdk` 无直接用例。

## 4. 测试用例设计 + 自动化实现
- 白盒：是接口、继承 `java.util.EventListener`、含两个方法。
- 黑盒（`testBlackBox()`）：匿名实现计数；headless 下以 `null` 作为 `Window` 参数调用两个回调（headless 无法构造 `Window`），断言回调确实被调用、计数正确、可重复调用。
