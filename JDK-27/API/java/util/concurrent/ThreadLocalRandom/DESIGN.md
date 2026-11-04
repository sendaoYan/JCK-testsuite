# java.util.concurrent.ThreadLocalRandom 设计案

## 1. API 功能
每线程随机数生成器（final class）。`current()` 取当前线程实例；`nextInt`/`nextLong`/`nextDouble` 带界随机；`ints`/`longs`/`doubles` 流。

## 2. Java 用户如何使用
多线程下用 `ThreadLocalRandom.current().nextInt(bound)` 免争用生成随机数；范围与流式生成。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ThreadLocalRandom`；`test/jdk/java/util/concurrent/ThreadLocalRandom/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 class/abstract/final/继承 + 反射核对方法与构造器签名。
2. 黑盒：真实实例化 + 多步组合操作断言（Future/队列/策略/随机范围等）。

实现：`ThreadLocalRandomTest.java`（`testWhiteBox` + `testBlackBox`）。
