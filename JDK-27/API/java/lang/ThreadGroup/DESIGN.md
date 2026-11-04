# ThreadGroup — 设计方案

## 1. API 功能
`java.lang.ThreadGroup` 组织线程为树状层级，实现 `Thread.UncaughtExceptionHandler`：
- 构造：`ThreadGroup(String)`、`ThreadGroup(ThreadGroup, String)`
- 查询：`getName/getParent/getMaxPriority/isDaemon/isDestroyed/parentOf`
- 管理：`setDaemon/setMaxPriority/interrupt/destroy/list`
- 枚举：`activeCount/enumerate(Thread[])/activeGroupCount/enumerate(ThreadGroup[])`

## 2. Java 用户如何使用
- 把相关线程放入同一组统一 `interrupt()` 或设优先级上限。
```java
ThreadGroup g = new ThreadGroup("workers");
g.setMaxPriority(Thread.NORM_PRIORITY);
new Thread(g, () -> work(), "w1").start();
int n = g.enumerate(arr, true);   // 列出组内活动线程
```

## 3. 开源代码实际用法调研
- 早期 servlet 容器/JVM 用 `ThreadGroup` 分组管理请求线程（现代多被 `ExecutorService` 取代）。
- JDK `test/jdk/java/lang/ThreadGroup` 验证 `destroy`、`enumerate`、优先级继承。
- `Thread.getThreadGroup()` 仍返回当前线程所属组。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.ThreadGroup")` 验证父类 `Object`、实现 `Thread.UncaughtExceptionHandler`。
黑盒：当前组属性；父子链接与 `parentOf`；`setMaxPriority`；`enumerate` 列出已启动 daemon 线程；`activeGroupCount`/`list`/`toString` 不抛；`destroy` 为 no-op、`isDestroyed` 恒 false（JDK 16+ 废弃语义）。
对应 `<Simple>Test.java` 黑盒部分。
