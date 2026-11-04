# java.util.concurrent.ConcurrentHashMap$KeySetView 设计案

## 1. API 功能
ConcurrentHashMap 的键集视图（final class，实现 Set）。`add`/`remove`/`contains`；`getMappedValue`/`getMap` 返回映射值与底层 map。

## 2. Java 用户如何使用
`ConcurrentHashMap.newKeySet()` 得到可添加的并发键集合；`map.keySet()` 得到只读键视图（mapped value 为 null，add 抛异常）。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ConcurrentHashMap.newKeySet()/keySet()`；`test/jdk/java/util/concurrent/ConcurrentHashMap/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 class/abstract/final/继承 + 反射核对方法与构造器签名。
2. 黑盒：真实实例化 + 多步组合操作断言（Future/队列/策略/随机范围等）。

实现：`KeySetViewTest.java`（`testWhiteBox` + `testBlackBox`）。
