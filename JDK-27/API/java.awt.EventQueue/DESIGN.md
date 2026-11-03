# java.awt.EventQueue 行为测试设计

## 1. API 功能
AWT 事件队列：postEvent 入队、peekEvent 窥视、getNextEvent 取出、dispatchEvent 分派（ActiveEvent 直接 dispatch()）；静态 invokeLater/invokeAndWait 把任务投到事件分派线程；isDispatchThread/getCurrentEvent/getMostRecentEventTime 查询分派线程状态。

## 2. Java 用户如何使用
典型用法：`EventQueue.invokeAndWait(() -> updateUI());` 保证在 EDT 上串行执行；`EventQueue.invokeLater` 异步投递；事件循环里 getNextEvent + dispatchEvent。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/EventQueue.java`（postEvent/peekEvent/dispatchEvent/invokeAndWait 实现）；`test/jdk/java/awt/EventQueue/` 与 Swing 大量 EDT 用例。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 Object、public 无参构造、peekEvent/postEvent/invokeLater/invokeAndWait 形态。黑盒：空队列 peekEvent 为 null；main 线程非 EDT、getCurrentEvent 为 null；invokeLater 在 EDT 上执行且 FIFO；invokeAndWait 同步返回并传播 InvocationTargetException；EDT 内 invokeAndWait 抛 Error；系统队列 postEvent/peekEvent/getNextEvent 基本语义（在 invokeAndWait 内同步验证）。
对应 `EventQueueTest.java` 的 `testWhiteBox()` / `testBlackBox()`。
