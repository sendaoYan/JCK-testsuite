# java.util.concurrent.ConcurrentHashMap 测试方案

## 1. API 功能（按方法分组）
- 构造器：`ConcurrentHashMap()`、`(int)`、`(Map)`、`(int,float)`、`(int,float,int)`。
- 核心操作：`put/get/remove(Object)/remove(Object,Object)/putIfAbsent/replace(K,V)/replace(K,old,new)/getOrDefault`。
- 原子默认方法：`computeIfAbsent/computeIfPresent/compute/merge`。
- 查询：`size()/mappingCount()/isEmpty()/containsKey/containsValue`。
- 视图/批量：`keySet()/values()/entrySet()`、`newKeySet()`、`forEach`、`reduce*/search*/forEachKey/Value/Entry`。
- 实现 `ConcurrentMap` + `Serializable`，extends `AbstractMap`。禁止 null key/value。

## 2. Java 用户如何使用
- 并发计数：`ConcurrentHashMap<String,LongAdder> freqs = new ConcurrentHashMap<>();`。
- 原子更新：`m.computeIfAbsent(k, init)`、`m.merge(k, 1, Integer::sum)`、`m.putIfAbsent(k, v)`。
- 并发集合视图：`ConcurrentHashMap.<String>newKeySet()` 得到并发 `Set`。
- 弱一致迭代：迭代中允许并发修改，不抛 `ConcurrentModificationException`。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `atomic/LongAdder.java:57-59`：文档示例 `ConcurrentHashMap<String,LongAdder> freqs` 做词频计数。
- JDK 源码 `package-info.java:187-202`：说明 `ConcurrentHashMap` 相比 `synchronized` HashMap 的并发优势与弱一致语义。
- JDK 测试 `test/jdk/java/util/concurrent/ConcurrentHashMap/`：覆盖 null 拒绝、原子方法、遍历弱一致、批量操作等。
- 使用范式：高并发缓存/计数/注册表，`putIfAbsent` 去重，`compute`/`merge` 原子复合更新。

## 4. 测试用例设计 + 自动化实现
实现位置：`ConcurrentHashMapTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractMap`/implements `ConcurrentMap`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - `put→get/containsKey/containsValue→remove→size/mappingCount→clear`。
  - null 拒绝：key/value 及 `get/containsKey/remove/putIfAbsent` 的 null 抛 `NullPointerException`。
  - 原子方法：`putIfAbsent`、`computeIfAbsent`（null 不插入）、`compute`（null 删除）、`merge`（null 删除）、`replace`。
  - 视图 `keySet/values/entrySet` + `newKeySet`。
  - 弱一致迭代：迭代中 `put/remove` 不抛 CME。
  - 批量 `forEach/reduceValues/searchValues/reduceKeys`。
- 健壮性：无 HashMap 迭代顺序断言（不依赖顺序）、固定输入、无全局副作用、headless-safe。
