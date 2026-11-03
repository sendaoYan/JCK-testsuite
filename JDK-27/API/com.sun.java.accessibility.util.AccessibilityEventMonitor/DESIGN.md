# AccessibilityEventMonitor 设计案

## 1. API 功能
`com.sun.java.accessibility.util.AccessibilityEventMonitor` 是一个「静态 PropertyChange 监听器注册器 + 事件监视器」：在 JVM 内所有实现 `Accessible` 的 UI 对象上安装 PropertyChange 监听器，并把事件汇集成一个统一的 `PropertyChangeListener` 集合。

- 构造器：`public AccessibilityEventMonitor()`。
- 注册/注销（`public static`）：`addPropertyChangeListener(PropertyChangeListener)`、`removePropertyChangeListener(PropertyChangeListener)`。
- 内部持有 `protected static final AccessibilityListenerList listenerList`。

## 2. Java 用户如何使用
辅助技术实现 `PropertyChangeListener` 并注册，即可集中收到所有 `Accessible` UI 对象的属性变更事件。

组合链路（来自 JDK 内部）：
```
AccessibilityEventMonitor.addPropertyChangeListener(myListener);
// UI 属性变化时内部 AccessibilityEventListener.propertyChange() 转发：
for (listener : listenerList.getListenerList()) listener.propertyChange(e);
AccessibilityEventMonitor.removePropertyChangeListener(myListener);
```

## 3. 开源代码实际用法调研
- Windows 辅助功能桥 `~/git/jdk25u-dev/src/jdk.accessibility/windows/classes/com/sun/java/accessibility/internal/AccessBridge.java` 第 5180/5218/5254/5268 行使用 `AccessibilityEventMonitor.addPropertyChangeListener(this)` / `removePropertyChangeListener(this)`。
- JDK 源码内部 `AccessibilityEventMonitor.java`：`addPropertyChangeListener` 在「该类型监听器数量为 0」时调用 `accessibilityListener.installListeners()`，`remove` 在数量归零时调用 `removeListeners()`；`AccessibilityEventListener` 实现 `TopLevelWindowListener` 以在新窗口出现时安装监听器。
- 行为权威：`~/git/jdk25u-dev/src/jdk.accessibility/share/classes/com/sun/java/accessibility/util/AccessibilityEventMonitor.java`。

## 4. 测试用例设计 + 自动化实现
- 白盒：类非接口、为具体类、有公有构造器；`add/removePropertyChangeListener` 方法存在。
- 黑盒（`testBlackBox()`）：headless 下：
  - `new AccessibilityEventMonitor()` 不抛。
  - 匿名 `PropertyChangeListener` 的 add → remove 不抛。
  - 组合场景：链式 add 两个监听器后逐个 remove，全程不抛（内部对空顶层窗口列表 no-op）。
