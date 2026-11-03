# java.util.concurrent.Future$State 设计案

## 1. API 功能
Future 状态枚举：`RUNNING`/`SUCCESS`/`FAILED`/`CANCELLED`。`Future.state()` 报告当前状态。

## 2. Java 用户如何使用
`future.state()` 判断任务未完成/成功/失败/已取消，避免阻塞式 `get()`。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.Future.state()` 默认实现与 `CompletableFuture.state()` 覆盖；`test/jdk/java/util/concurrent/Future/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：用具体实现（LinkedBlockingQueue/ConcurrentHashMap/Executor 族等）走多步组合操作并断言可观察行为。

实现：`StateTest.java`（`testWhiteBox` + `testBlackBox`）。
