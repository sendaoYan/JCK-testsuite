# java.util.concurrent.ConcurrentHashMap 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（5）**：`()`、`(int)`、`(Map)`、`(int,float)`、`(int,float,int)`。
- **核心操作**：`put`/`get`/`remove`、`putAll`、`clear`、`containsKey`/`containsValue`。
- **原子默认方法**：`putIfAbsent`、`remove(K,V)`、`replace(K,V)`、`replace(K,old,new)`、`getOrDefault`、
  `computeIfAbsent`、`computeIfPresent`、`compute`、`merge`、`forEach`、`replaceAll`。
- **视图**：`keySet`、`values`、`entrySet`、`keySet(V)`、静态 `newKeySet`/`newKeySet(int)`。
- **弱一致迭代**：`keys()`/`elements()` 枚举。
- **bulk 并行**：`forEach/search/reduce` 及 `forEachKey/Value/Entry`、`searchKeys/Values/Entries`、
  `reduceKeys/Values/Entries`、`reduceToInt/Long/Double` 系列。
- **查询**：`size`、`mappingCount`。
- **契约**：`extends AbstractMap` + `implements ConcurrentMap` + `Serializable`。

## 2. Java 用户如何使用

- 线程安全 HashMap 替代；禁止 null key/value（抛 NPE）。
- 原子复合更新：`computeIfAbsent`/`compute`/`merge`。
- `forEach/search/reduce` 做并行批量遍历/聚合；`newKeySet` 提供并发 Set 视图。
- 迭代器弱一致：迭代中修改不抛 CME。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/ConcurrentHashMap.html`）。
- OpenJDK `test/jdk/java/util/concurrent/`（ConcurrentHashMap 回归）。
- JCK runtime-8b `api/java_util_concurrent/ConcurrentHashMap`。

## 4. 测试用例设计（映射到 ConcurrentHashMapTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | put/get/remove/size/mappingCount/clear | testPutGetRemove |
| P0 | null 拒绝矩阵（key/value/查询/默认方法） | testNullRejection |
| P0 | putIfAbsent/computeIfAbsent/compute/merge/replace/getOrDefault 深分支 | testAtomicOperations |
| P0 | keySet/values/entrySet + newKeySet 语义 | testViewsAndNewKeySet |
| P0 | 弱一致迭代器（迭代中 put/remove 不抛 CME） | testWeaklyConsistentIterator |
| P0 | forEach/reduceValues/searchValues/reduceKeysToInt | testBulkOperations |
| P0 | 构造器矩阵/putAll/computeIfPresent/replaceAll/keys/elements | testBlackBox |
| P0 | equals/hashCode 与 HashMap 对称 | testBlackBox |
| P0 | search/searchKeys/searchEntries + reduce/reduceKeys/reduceEntries + reduceTo*/reduce*To* 全量 | testBlackBox |
| P0 | forEachKey/Value/Entry 两重载 | testBlackBox |
| P1 | 白盒结构 | testWhiteBox |

## 关键规范细节

- 禁止 null key 和 null value（所有读写/默认方法抛 NPE）。
- 迭代器弱一致：允许并发修改，不抛 ConcurrentModificationException。
- `computeIfAbsent` 函数返回 null 不插入；`compute`/`merge` 返回 null 删映射。
- bulk 操作的 `parallelismThreshold` 参数 `1` 表示尽量单线程确定执行。
