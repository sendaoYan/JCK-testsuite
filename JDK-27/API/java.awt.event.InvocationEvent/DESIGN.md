# java.awt.event.InvocationEvent 行为测试设计

## 1. API 功能
把 Runnable 封装成可入队/分派的事件（实现 ActiveEvent）。dispatch() 执行 runnable，记录 when 时间戳，支持 notifier 唤醒与可选的异常捕获（catchExceptions/getException/getThrowable），isDispatched 标记完成。

## 2. Java 用户如何使用
典型用法：`new InvocationEvent(source, runnable, notifier, catchThrowables)` 后由 EventQueue 分派；invokeAndWait 内部用它同步等待。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/InvocationEvent.java`；`EventQueue.java`（invokeAndWait 用 notifier.wait + isDispatched 循环）。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 AWTEvent、实现 ActiveEvent、常量与构造器。黑盒：getWhen 非负；isDispatched 初始 false、dispatch 后 true；dispatch 执行 runnable 副作用；默认不捕获异常会传播；catchExceptions=true 时 getException/getThrowable 保存异常；listener 构造器在 dispatch 后回调；paramString 含字段。
对应 `InvocationEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。
