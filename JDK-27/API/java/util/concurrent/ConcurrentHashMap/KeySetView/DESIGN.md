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

## 4.1 契约深化追加（第二轮，PASS 22 → 42 断言）

| 优先级 | 测试点 | 用例方法（追加） |
|---|---|---|
| P0 | **mappedValue 语义**：`map.keySet(42).add(k)` == `map.put(k,42)`；键已存在返回 false 且**不覆盖**原值（putIfAbsent 式）；`getMappedValue()==42`；`map.keySet()`（无参）视图 `add` 抛 UOE；`newKeySet()`/`newKeySet(int)` 的 mappedValue 为 `Boolean.TRUE` | testMappedValueSemantics |
| P0 | 视图迭代内容与底层 map 同步、contains/size/remove 全直通、addAll 批量写通道 | testViewIterationAndBulk |
| P0 | 序列化 round-trip（mappedValue 参与序列化，反序列化后 `Boolean.TRUE.equals(getMappedValue())`——是 equals 不是 ==，反序列化的 Boolean 非同一实例） | testSerialization |

### 易错规范细节（实测记录）

- KeySetView 在 JDK-27 为 final class，且 `removeAll` 为 public 重写——白盒已断言 final 修饰。
- `keySet(v).add(k)` 对**已存在**键返回 false 且不动原值（与 put 不同、与 putIfAbsent 相同）。
- newKeySet 的默认 mappedValue 是 `Boolean.TRUE`（装箱单例）；序列化后用 `Boolean.TRUE.equals(...)` 断言，不能 `==`。
