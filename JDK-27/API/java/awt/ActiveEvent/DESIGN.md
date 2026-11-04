# java.awt.ActiveEvent 黑盒行为测试设计

## 1. API 功能
- 接口：单方法 `void dispatch()`。
- 用于「已排队的事件」在 `EventQueue` 派发时执行的动作。

## 2. Java 用户如何使用
实现 `ActiveEvent`，由 `EventQueue.invokeAndWait/invokeLater` 执行：
```java
ActiveEvent e = new ActiveEvent() { public void dispatch() { doWork(); } };
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/ActiveEvent.java`（接口定义）。
- `src/java.desktop/share/classes/java/awt/EventQueue.java`、`java/awt/event/InvocationEvent.java`：`InvocationEvent implements ActiveEvent`，`dispatch()` 调 Runnable。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 接口白盒：单抽象方法、返回 void。
- 匿名实现：`dispatch()` 前后翻转标志、可重复调用、计数。
headless-safe。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：接口方法；最小实现 dispatch 计数；可重复调用。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
