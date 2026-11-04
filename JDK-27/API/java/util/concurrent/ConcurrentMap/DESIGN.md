# java.util.concurrent.ConcurrentMap 设计案

## 1. API 功能
线程安全 Map 接口（继承 Map）。原子 `putIfAbsent`/`remove(k,v)`/`replace` 以及 `compute*`/`merge`/`getOrDefault` 默认方法。

## 2. Java 用户如何使用
并发读写 Map：`putIfAbsent` 无锁插入、`computeIfAbsent` 惰性初始化、`merge` 聚合计数。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ConcurrentHashMap` 实现；`test/jdk/java/util/concurrent/ConcurrentMap/` 与 tck 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：用具体实现（LinkedBlockingQueue/ConcurrentHashMap/Executor 族等）走多步组合操作并断言可观察行为。

实现：`ConcurrentMapTest.java`（`testWhiteBox` + `testBlackBox`）。
