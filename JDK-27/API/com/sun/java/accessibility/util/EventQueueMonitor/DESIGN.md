# EventQueueMonitor 设计案

## 1. API 功能
`com.sun.java.accessibility.util.EventQueueMonitor` 是辅助功能栈的核心监视器，实现 `java.awt.event.AWTEventListener`，缓存顶层窗口、当前鼠标位置、聚焦窗口、GUI 初始化状态，并提供静态查询与监听器注册。

- 构造器：`public EventQueueMonitor()`（首次构造会启动一个 daemon 线程 `ComponentEvtDispatchThread`）。
- 初始化：`public static void maybeInitialize()`；`public void eventDispatched(AWTEvent)`。
- 查询：`isGUIInitialized()`、`getAccessibleAt(Point)`、`getCurrentMousePosition()`、`getTopLevelWindows()`、`getTopLevelWindowWithFocus()`。
- 注册：`add/removeGUIInitializedListener`、`add/removeTopLevelWindowListener`。

## 2. Java 用户如何使用
辅助技术通过 `EventQueueMonitor` 的静态方法获取 GUI 全局状态，并在合适时机注册监听器等待 GUI 初始化。

组合链路（来自 JDK 内部 `AccessBridge.java`）：
```
EventQueueMonitor.isGUIInitialized();
Accessible a = EventQueueMonitor.getAccessibleAt(new Point(x, y));
```

## 3. 开源代码实际用法调研
- Windows 辅助功能桥 `~/git/jdk25u-dev/src/jdk.accessibility/windows/classes/com/sun/java/accessibility/internal/AccessBridge.java` 第 207 行调用 `EventQueueMonitor.isGUIInitialized()`、第 634 行调用 `EventQueueMonitor.getAccessibleAt(new Point(x, y))`。
- JDK 源码内部 `EventQueueMonitor.java`：`maybeInitialize()` 通过 `Toolkit.getDefaultToolkit().addAWTEventListener(new EventQueueMonitor(), mask)`（整体 try/catch 包裹，headless 安全）；`getTopLevelWindows()` 返回空数组当无窗口；`getAccessibleAt` 在 `currentMousePosition == null` 时直接返回 null。
- 行为权威：`~/git/jdk25u-dev/src/jdk.accessibility/share/classes/com/sun/java/accessibility/util/EventQueueMonitor.java`。

## 4. 测试用例设计 + 自动化实现
- 白盒：类非接口、为具体类、有公有构造器、实现 `AWTEventListener`；各方法名存在（保留旧断言并补齐 `getTopLevelWindowWithFocus`）。
- 黑盒（`testBlackBox()`）：headless 下：
  - `new EventQueueMonitor()` 不抛（daemon 线程不会挂起 jtreg）。
  - `isGUIInitialized()` 为 false；`maybeInitialize()` 后仍为 false（headless、无事件）。
  - `getCurrentMousePosition()` null、`getTopLevelWindows()` 非空且长度 0、`getTopLevelWindowWithFocus()` null、`getAccessibleAt(new Point(0,0))` null。
  - `add/removeGUIInitializedListener`、`add/removeTopLevelWindowListener` 不抛。
  - `eventDispatched` 用良性 `ActionEvent`（非窗口/焦点/鼠标事件）调用不抛、鼠标位置仍 null。
