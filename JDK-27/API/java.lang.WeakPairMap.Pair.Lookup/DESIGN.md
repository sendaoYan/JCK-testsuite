# WeakPairMap.Pair.Lookup — 设计方案

## 1. API 功能
`java.lang.WeakPairMap.Pair.Lookup<K1, K2>`（public final，实现包私有接口 `WeakPairMap.Pair`）是 `WeakPairMap` 的查找键：持有强引用的 `first()/second()`，`hashCode/equals` 按 (k1, k2) 值语义匹配 Map 中经 `ReferenceQueue` 弱化的键对。

## 2. Java 用户如何使用
- 非导出内部 API：`WeakPairMap` 与 `Pair` 均为包私有（javap：`final class java.lang.WeakPairMap`、`interface java.lang.WeakPairMap$Pair`），构造器包私有、`Pair.lookup(...)` 工厂在不可达接口上，外部代码无法获得实例；仅 `java.lang` 内部（ThreadLocal/Thread 实现细节）使用。

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/Thread` 侧无直接用例（内部机制随 JDK 19 虚拟线程/ThreadLocal 改造引入）；无第三方公开使用（类型虽 public 但无法实例化）。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.WeakPairMap$Pair$Lookup")` 验证 public final、实现 `WeakPairMap$Pair`、构造器包私有、`first/second/hashCode/equals` 均公开且泛型擦除为 Object。
黑盒：受包私有限制（无公开工厂/构造器）无法实例化，仅验证 bootstrap 加载、java.lang 包、非接口/枚举、双泛型参数，并在测试注释与本设计说明原因。
对应 `LookupTest.java` 白盒 + 黑盒部分。
