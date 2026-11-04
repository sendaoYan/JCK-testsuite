# GUIInitializedListener 设计案

## 1. API 功能
`com.sun.java.accessibility.util.GUIInitializedListener` 是一个标记回调接口（`extends java.util.EventListener`），供辅助技术监听「GUI 子系统已初始化」这一事件。

- 单抽象方法：`void guiInitialized()`。
- 无构造器（接口）。

## 2. Java 用户如何使用
辅助技术实现该接口并注册到 `EventQueueMonitor.addGUIInitializedListener(...)`；当 `EventQueueMonitor` 首次收到 AWT 事件并调用 `maybeNotifyAssistiveTechnologies()` 时回调。

组合链路（来自 JDK 内部）：
```
EventQueueMonitor.addGUIInitializedListener(myListener); // 注册
// GUI 初始化后 EventQueueMonitor 回调：
myListener.guiInitialized();                             // 通知
```

## 3. 开源代码实际用法调研
- JDK 源码内部：`~/git/jdk25u-dev/src/jdk.accessibility/share/classes/com/sun/java/accessibility/util/EventQueueMonitor.java` 维护 `static GUIInitializedListener guiInitializedListener`，在 `addGUIInitializedListener/removeGUIInitializedListener` 中通过 `GUIInitializedMulticaster` 增删，并在 `maybeNotifyAssistiveTechnologies()` 中回调 `guiInitializedListener.guiInitialized()`。
- 行为权威：`~/git/jdk25u-dev/src/jdk.accessibility/share/classes/com/sun/java/accessibility/util/GUIInitializedListener.java` 仅声明一个方法 `guiInitialized()`。
- 注：本接口为功能接口（SAM），可用 lambda 或匿名类实现；`test/jdk` 无直接用例。

## 4. 测试用例设计 + 自动化实现
- 白盒：是接口、继承 `java.util.EventListener`、含 `guiInitialized` 方法。
- 黑盒（`testBlackBox()`）：匿名实现记录调用状态；断言初始未调用、`guiInitialized()` 调用后置位、可重复调用不抛异常。
