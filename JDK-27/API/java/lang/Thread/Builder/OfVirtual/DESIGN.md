# Thread.Builder.OfVirtual — 设计方案

## 1. API 功能
`java.lang.Thread.Builder.OfVirtual`（接口，继承 `Thread.Builder`）专用于虚拟线程：仅 `name(String)`、`name(String, long)`、`inheritInheritableThreadLocals(boolean)`、`uncaughtExceptionHandler(...)`（协变返回 `OfVirtual`），不含 group/priority/stackSize/daemon 等平台线程专属选项。

## 2. Java 用户如何使用
- 高并发任务直接创建/工厂化虚拟线程：
```java
ThreadFactory f = Thread.ofVirtual().name("req-", 0).factory();
```

## 3. 开源代码实际用法调研
- JEP 444/Java 21+；JDK `test/jdk/java/lang/ThreadBuilders` 验证虚拟线程名、未捕获处理器、工厂计数；Helidon/Loom 早期采用者以 `ofVirtual().factory()` 作为 Executor 默认线程工厂。

## 4. 测试用例设计 + 自动化实现
白盒：验证接口继承 `Thread.Builder`、协变返回；遍历自有方法确认无 group/priority/stackSize/daemon。
黑盒：`start` 得到虚拟线程（isVirtual、恒 daemon、线程组名 VirtualThreads、join 后 TERMINATED）；`unstarted` 为 NEW；`factory()` 前缀+计数（vf-3/vf-4）且产出虚拟线程；未捕获异常处理器收到死亡线程与异常。
对应 `OfVirtualTest.java` 白盒 + 黑盒部分。
