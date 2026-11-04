# EventListenerList 行为测试设计

## 1. API 功能
`javax.swing.event.EventListenerList` 是 Swing 内部用「类型-监听器」平铺数组维护的监听器容器，实现 `java.io.Serializable`。

- **构造器**：`EventListenerList()`（无参）。
- **添加/移除**：`add(Class<T>, T)`、`remove(Class<T>, T)`（null 为 no-op；类型不匹配抛 `IllegalArgumentException`）。
- **查询**：`getListenerList()`（返回内部平铺数组，顺序为插入序）、`getListeners(Class<T>)`（按逆插入序返回该类型监听器）、`getListenerCount()`（总数）、`getListenerCount(Class<?>)`（指定类型数）、`toString()`。

## 2. Java 用户如何使用
组件类用它在注册/派发监听器时统一管理：

```java
EventListenerList listenerList = new EventListenerList();
listenerList.add(ChangeListener.class, changeListener);
listenerList.add(ActionListener.class, actionListener);
// 派发时逆序遍历
ChangeListener[] ls = listenerList.getListeners(ChangeListener.class);
for (ChangeListener l : ls) l.stateChanged(ev);
```

每个方法典型调用：`add(type, l)` 注册 → `getListenerCount(type)` 计数 → `getListeners(type)` 批量派发 → `remove(type, l)` 注销 → `getListenerList()` 手工遍历（如 RowSorter 内部）。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/RowSorter.java`：
   ```java
   Object[] listeners = listenerList.getListenerList();
   for (int i = listeners.length - 2; i >= 0; i -= 2) {
       if (listeners[i] == RowSorterListener.class) { ... }
   }
   ```
   直接遍历 `getListenerList()` 的「类型-监听器」平铺对。
2. `test/jdk/javax/swing/event/EventListenerList/GetUpToDateData.java`：
   ```java
   listeners.add(EventListener.class, o1);
   listeners.add(AWTEventListener.class, o2);
   listeners.getListeners(EventListener.class)[0] == o1;
   ```
   验证 `getListeners`/`getListenerCount` 返回最新数据。
3. `jdk25u-dev/src/java.desktop/share/classes/java/awt/AWTEventMulticaster.java` 也以 EventListenerList 存储 AWT 监听器。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 Object、public、非 abstract、接口只有 Serializable）。
- 黑盒：初始空容器 → 添加两个 ChangeListener + 一个 ActionListener → 验证总数/分类计数/平铺数组顺序（插入序）→ `getListeners` 逆序 → `remove` 后计数更新 → 清空后回退到空数组 → `add(type, null)` 为 no-op → 类型不匹配抛 `IllegalArgumentException`。
- 自动化：`testWhiteBox()` + `testBlackBox()`，多步组合操作，headless-safe。

## 5. 深化追加（第二轮）
- 重复注册：同一 listener add 两次计数为 2、typed 数组可见 2 份；remove 一次后仍剩 1 份。
- 反例：remove 从未注册的 listener 是文档化 no-op（计数不变）。
- 未注册类型：`getListeners`/`getListenerCount` 返回空/0。
- 序列化 round-trip：listener 为可序列化静态类时，计数/typed 数组/元素类型全部保持。
