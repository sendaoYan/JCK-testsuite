# Thread.Builder.OfPlatform — 设计方案

## 1. API 功能
`java.lang.Thread.Builder.OfPlatform`（接口，继承 `Thread.Builder`）专用于平台线程，追加 `group(ThreadGroup)`、`daemon(boolean)`/`daemon()`（default）、`priority(int)`、`stackSize(long)`；各方法协变返回 `OfPlatform` 以保持链式调用。

## 2. Java 用户如何使用
- 创建带组/优先级/守护标志/栈大小的平台线程或线程工厂：
```java
Thread t = Thread.ofPlatform().group(g).daemon().priority(7).stackSize(1 << 20).start(task);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/ThreadBuilders` 验证 group/daemon/priority/stackSize 与协变返回；Netty `ThreadPerChannelEventLoop` 类构建器使用守护平台线程。

## 4. 测试用例设计 + 自动化实现
白盒：验证接口继承 `Thread.Builder`、`name/group/daemon(boolean)/daemon()/priority/stackSize` 协变返回 `OfPlatform`、`daemon()` 为 default 方法。
黑盒：组合 group/name/daemon/priority/stackSize(0) 后线程属性一致、非虚拟、终止后退出线程组；`daemon()` 默认 true、`daemon(false)` 显式；`factory()` 产出 NEW 状态线程且带前缀+计数名。
对应 `OfPlatformTest.java` 白盒 + 黑盒部分。
