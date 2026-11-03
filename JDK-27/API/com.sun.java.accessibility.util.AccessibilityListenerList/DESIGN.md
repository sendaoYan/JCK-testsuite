# AccessibilityListenerList 设计案

## 1. API 功能
`com.sun.java.accessibility.util.AccessibilityListenerList` 是 Swing `javax.swing.event.EventListenerList` 的拷贝版，用「类型-监听器」成对数组维护监听器集合，供辅助功能模块保存/分发监听器。

- 构造器：`public AccessibilityListenerList()`。
- 查询：`getListenerList()`（返回内部 `Object[]` 原始数组，长度=监听器数×2）、`getListenerCount()`、`getListenerCount(Class<? extends EventListener>)`、`toString()`。
- 修改：`add(Class<? extends EventListener>, EventListener)`、`remove(Class, EventListener)`（均 `synchronized`）。
- 约束：`add/remove` 对「类型不匹配的监听器」或「null 监听器」抛 `IllegalArgumentException`。

## 2. Java 用户如何使用
用户创建列表后按类型注册监听器，触发事件时遍历 `getListenerList()` 分发给对应类型的监听器，不再需要时 `remove`。

组合链路：
```
AccessibilityListenerList l = new AccessibilityListenerList();
l.add(ActionListener.class, al);      // 注册
l.add(EventListener.class, el);       // 注册另一类型
l.getListenerCount(ActionListener.class); // 按类型计数
l.getListenerList();                  // 取出 [type, listener, ...] 数组
l.remove(ActionListener.class, al);   // 注销
```

## 3. 开源代码实际用法调研
- JDK 源码内部：`~/git/jdk25u-dev/src/jdk.accessibility/share/classes/com/sun/java/accessibility/util/AccessibilityEventMonitor.java` 使用 `protected static final AccessibilityListenerList listenerList`，通过 `listenerList.getListenerCount(PropertyChangeListener.class)`、`listenerList.add(...)`、`listenerList.getListenerList()`、`listenerList.remove(...)` 维护全局 PropertyChange 监听器集合。
- 行为权威：`~/git/jdk25u-dev/src/jdk.accessibility/share/classes/com/sun/java/accessibility/util/AccessibilityListenerList.java` 给出 `add/remove/count/toString` 的精确语义（允许重复注册、remove 从头（从尾）查找并移除第一个匹配项、数组回退到共享空数组）。
- 注：本类未在 `AccessBridge.java` 中被直接引用，属于模块内部基础设施；行为以源码为准，不凭记忆。

## 4. 测试用例设计 + 自动化实现
- 白盒：类非接口、为具体类、有公有构造器；`add/remove/getListenerList/getListenerCount/toString` 方法存在。
- 黑盒（`testBlackBox()`）：
  - 初始 count=0、`getListenerList()` 非空且长度 0、`toString()` 前缀 `EventListenerList:`。
  - `add` 两种类型监听器 → count/按类型 count/原始数组长度与内容正确。
  - `remove` 后剩余监听器不受影响，最终回到空数组。
  - `remove` 不存在的监听器为 no-op。
  - 类型不匹配 / null 监听器 → `IllegalArgumentException`。
