# java.util.concurrent.ConcurrentNavigableMap 设计案

## 1. API 功能
线程安全有序 Map 接口（继承 ConcurrentMap + NavigableMap）。支持子区间 `subMap`/`headMap`/`tailMap`、`descendingMap`、有序键视图。

## 2. Java 用户如何使用
并发有序键范围查询：`ceilingKey`/`floorKey` 找最近键、`subMap` 区间遍历。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ConcurrentSkipListMap` 实现；`test/jdk/java/util/concurrent/ConcurrentSkipListMap/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：用具体实现（LinkedBlockingQueue/ConcurrentHashMap/Executor 族等）走多步组合操作并断言可观察行为。

实现：`ConcurrentNavigableMapTest.java`（`testWhiteBox` + `testBlackBox`）。
