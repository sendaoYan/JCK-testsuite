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

## 4.1 契约深化追加（第二轮，PASS 115 → 219 断言）

追加方法（全部保留原有用例）：

| 优先级 | 测试点 | 用例方法（追加） |
|---|---|---|
| P0 | **构造器矩阵**：(-1)/(-1,·)/0f/-1f/NaN/(·,·,0)/(·,·,-3) 全 IAE、(0) 与 (MAX_VALUE) 合法、ctor(null) NPE | testConstructorMatrix |
| P0 | **null 全拒矩阵（补全）**：containsValue(null)/contains(null)/remove(null,v)/replace 三形式 null/getOrDefault(null)/computeIfPresent(null)/compute 系 null fn/merge 两向 null/putAll(null)/putAll 含 null/视图 contains(null) 全 NPE；**remove(k,null) 不抛返回 false** | testNullRejectionFull |
| P0 | **compute/merge 全分支矩阵**（函数调用计数器证明：present 不调 fn、merge absent 不调 fn；返回 null 删/不落 entry；异常对象透传且映射不变） | testComputeMergeBranchMatrix |
| P0 | **视图写通道**：keySet/values/entrySet remove 直通；entrySet().add 具 put 语义（新键 true、已存在 false 但覆盖）；entry 迭代器 setValue 直通 | testViewsWriteThrough |
| P0 | **keySet(42) 默认值语义**：add==put(k,42)、已存在不覆盖、getMappedValue、newKeySet→Boolean.TRUE | testKeySetViewMapped |
| P0 | **迭代弱一致契约强化**：取迭代器后 put+remove 继续遍历不抛 CME（对照断言 ArrayList 同场景抛 CME） | testIteratorWeakConsistencyContract |
| P0 | 序列化 round-trip + serialVersionUID 锁定（7249069246763182397L） | testSerializationRoundTrip |
| P0 | size==mappingCount（100 元素跨扩容）+ 扩容后 compute/merge 分支 | testSizeMappingCountResize |
| P1 | equals/hashCode/toString 内容契约（与 HashMap 跨实现对称；toString 只断内容因 CHM 无序） | testEqualsToStringContract |
| P1 | 有界并发 put 一致性（daemon 线程 + latch ≤5s，2×500 键全可见） | testConcurrentPutConsistency |

### 易错规范细节（第二轮实测记录）

- `remove(k, null)` 不抛——null 永不可能是已存值，直接返回 false 且映射不变（其余 null 全 NPE）。
- `entrySet().add(entry)` 是 CHM 扩展：**put 语义**（新键 true；已存在 false 但值覆盖）；`keySet().add`（无默认值）/`values().add` 抛 UOE。
- entrySet 迭代器 handed-out Entry 支持 setValue 且直通 map；先取 Entry 后对同键 put 不改已取 Entry（值快照）。
- 异常消息与 JDK-8 不同（J27 有详细消息、J8 为 null）——只断言异常类型。
- serialVersionUID = 7249069246763182397L（与 JDK-8 一致）。
