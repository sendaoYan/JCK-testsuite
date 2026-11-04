# SwingEventMonitor 设计案

## 1. API 功能
`com.sun.java.accessibility.util.SwingEventMonitor` 继承 `AWTEventMonitor`，在其基础上追加 Swing 组件事件的统一监听器集合。既监控 AWT 又监控 Swing 时只需使用本类。

- 构造器：`public SwingEventMonitor()`。
- 继承的查询：`getComponentWithFocus()`（来自 `AWTEventMonitor`）。
- Swing 注册/注销（`public static`）：`Ancestor/Caret/CellEditor/Change/ColumnModel/Document/ListData/ListSelection/Menu/PopupMenu/TableModel/TreeExpansion/TreeModel/TreeSelection/UndoableEdit/InternalFrame` 监听器，外加 `PropertyChangeListener` / `VetoableChangeListener`。

## 2. Java 用户如何使用
用户静态调用 `addXxxListener(...)` 注册对应 Swing 事件监听器，`removeXxxListener(...)` 注销；内部 `SwingEventListener`（继承 `AWTEventsListener`）把事件转发给已注册监听器。

组合链路（来自 JDK 内部 `AccessBridge.java`）：
```
SwingEventMonitor.addFocusListener(this);
SwingEventMonitor.addCaretListener(this);
SwingEventMonitor.addMouseListener(this);
SwingEventMonitor.addMenuListener(this);
SwingEventMonitor.addPopupMenuListener(this);
...
SwingEventMonitor.removeFocusListener(this);
```

## 3. 开源代码实际用法调研
- Windows 辅助功能桥 `~/git/jdk25u-dev/src/jdk.accessibility/windows/classes/com/sun/java/accessibility/internal/AccessBridge.java` 第 5185–5223 行大量使用 `SwingEventMonitor.add/removeFocusListener、addCaretListener、addMouseListener、addMenuListener、addPopupMenuListener`。
- JDK 源码内部 `SwingEventMonitor.java`：`listenerList` 是 `javax.swing.event.EventListenerList`，`addXxx` 在「该类型监听器数量从 0 变 1」时调用 `swingListener.installListeners(EventID.XXX)`，`removeXxx` 在数量归零时调用 `removeListeners`。
- 行为权威：`~/git/jdk25u-dev/src/jdk.accessibility/share/classes/com/sun/java/accessibility/util/SwingEventMonitor.java`。

## 4. 测试用例设计 + 自动化实现
- 白盒：类非接口、为具体类、继承 `AWTEventMonitor`；18 类 Swing 监听器方法名存在（保留旧断言并补齐全部）。
- 黑盒（`testBlackBox()`）：headless 下：
  - `new SwingEventMonitor()` 不抛；继承的 `getComponentWithFocus()` 返回 null。
  - 逐类对 18 类 Swing 监听器执行「匿名实现 add → remove」，全程不抛、不触碰 DISPLAY。
  - 组合场景：跨类别同时 add 多个监听器后逐个 remove，校验 focus 仍 null。
  - 结论：所有 add/remove 在 headless 下均安全。
