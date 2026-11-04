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

## 4.1 契约深化追加（第二轮，PASS 112 → 224 断言）

| 优先级 | 测试点 | 用例方法（追加） |
|---|---|---|
| P0 | **构造器矩阵**：(-1)/(-1,·)/0f/-1f/NaN/(·,·,0)/(·,·,-3) 全 IAE、(0) 与 (MAX_VALUE) 合法、ctor(null) NPE | testConstructorMatrix |
| P0 | **null 全拒矩阵（补全）**：containsValue(null)/contains(null)/remove(null,v)/replace 三形式 null/getOrDefault(null)/computeIfPresent(null)/compute 系 null fn/merge 两向 null/putAll(null)/putAll 含 null/视图 contains(null) 全 NPE；**remove(k,null) 不抛返回 false** | testNullRejectionFull |
| P0 | **compute/merge 全分支矩阵**（函数调用计数器证明：present 不调 fn、merge absent 不调 fn；返回 null 删/不落 entry；异常对象透传且映射不变） | testComputeMergeBranchMatrix |
| P0 | **视图写通道**：keySet/values/entrySet remove 直通；entrySet().add 具 put 语义（新键 true、已存在 false 但覆盖）；entry 迭代器 setValue 直通 | testViewsWriteThrough |
| P0 | **keySet(42) 默认值语义**：add==put(k,42)、已存在不覆盖、getMappedValue、newKeySet→Boolean.TRUE | testKeySetViewMapped |
| P0 | **迭代弱一致契约强化**：取迭代器后 put+remove 继续遍历不抛 CME（对照断言 ArrayList 同场景抛 CME） | testIteratorWeakConsistencyContract |
| P0 | 序列化 round-trip + serialVersionUID 锁定（7249069246763182397L） | testSerializationRoundTrip |
| P0 | size==mappingCount（100 元素跨扩容）+ 扩容后 compute/merge 分支（交叉维度） | testSizeMappingCountResize |
| P1 | equals/hashCode/toString 内容契约（与 HashMap 跨实现对称；toString 只断内容因 CHM 无序） | testEqualsToStringContract |
| P1 | 有界并发 put 一致性（daemon 线程 + latch ≤5s，2×500 键全可见） | testConcurrentPutConsistency |

## 关键规范细节（含第二轮实测记录）

- 禁止 null key 和 null value（所有读写/默认方法抛 NPE）。**特例**：`remove(k, null)` 不抛——null 永远不可能是已存值，直接返回 false 且映射不变。
- `entrySet().add(entry)` 是 CHM 扩展：具 **put 语义**（新键返回 true；键已存在返回 false 但值被覆盖）；`keySet().add`（无默认值）抛 UOE；`values().add` 抛 UOE。
- entrySet 迭代器 handed-out 的 Entry 支持 setValue 且直通 map。
- 迭代器弱一致：允许并发修改，不抛 ConcurrentModificationException（这是契约，不是实现巧合）。
- `computeIfAbsent` 函数返回 null 不插入；`compute`/`computeIfPresent`/`merge` 返回 null 删映射。
- 构造器 IAE/NPE 的消息在 JDK-8 侧为 null、JDK-27 侧有详细消息——只断言异常类型。
- bulk 操作的 `parallelismThreshold` 参数 `1` 表示尽量单线程确定执行。
- serialVersionUID = 7249069246763182397L（双侧一致）。
