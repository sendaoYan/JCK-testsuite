# java.util.LinkedHashMap 测试方案

## 1. API 功能（按方法分组）

- 构造器：`LinkedHashMap()`、`LinkedHashMap(int initialCapacity)`、`LinkedHashMap(int, float loadFactor)`、`LinkedHashMap(int, float, boolean accessOrder)`、`LinkedHashMap(Map)`。
- 核心操作：`put`/`get`/`remove`/`clear`（继承自 HashMap，但维护双向链表的顺序）。
- 顺序相关：`removeEldestEntry(Map.Entry)`（protected，LRU 钩子）、`reversed()`（SequencedMap）。
- SequencedMap 首尾：`putFirst(K,V)`、`putLast(K,V)`、`firstEntry()`、`lastEntry()`、`pollFirstEntry()`、`pollLastEntry()`。
- 继承默认方法：`computeIfAbsent`/`compute`/`merge`/`replaceAll`/`forEach`/`putIfAbsent`/`getOrDefault`。

## 2. Java 用户如何使用

- 构造：`new LinkedHashMap<>()`（插入序）；`new LinkedHashMap<>(16, 0.75f, true)`（访问序 LRU）。
- 典型调用：
  - 保序：插入后按插入序迭代（缓存/配置项展示）。
  - LRU 淘汰：匿名子类覆盖 `removeEldestEntry` 返回 `size() > max`。
  - 首尾操作：`putFirst`/`putLast`/`pollFirstEntry`/`pollLastEntry`。
- 真实组合链路：
  1. 插入 a,b,c → 重复 put 不改变位置 → get 不重排（插入序）。
  2. 访问序模式下 get/put 更新都算访问，把条目移到末尾。
  3. 覆盖 `removeEldestEntry` 做容量淘汰（验证淘汰的是真正最老条目）。
  4. `putFirst/putLast` 定位 → `firstEntry/lastEntry` → `pollFirstEntry/pollLastEntry` → `reversed()` 倒序视图。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 测试 `test/jdk/java/util/LinkedHashMap/Cache.java:39`：覆盖 `removeEldestEntry` 实现 LRU 缓存，
  验证访问序下淘汰最久未访问条目。
- JDK 源码 `java.util.Collections.java:6115-6121`（`newSequencedSetFromMap` 的 javadoc 代码片段）：
  `new LinkedHashMap<String, Boolean>() { protected boolean removeEldestEntry(Map.Entry<String,Boolean> e) { return this.size() > 5; } }`，
  说明 LinkedHashMap 可作有序 Set 的底层存储并附带淘汰策略。
- 通用开源模式（Guava/Spring 文档与 `test/jdk` 共识）：`LinkedHashMap(int, float, true)` + `removeEldestEntry` 是经典 LRU 实现；
  生产环境 Spring 更推荐 Caffeine/Guava Cache（更强并发/过期策略），但 LinkedHashMap 仍是轻量 LRU 的惯用法。

## 4. 测试用例设计 + 自动化实现

实现位置：`LinkedHashMapTest.java`（`testWhiteBox()` + `testInsertionOrder()`/`testAccessOrder()`/`testRemoveEldestEntry()`/`testSequencedMapOps()`）。

- 正常：插入序保持、重复 put 更新值不移动、null key/value 允许。
- 组合：访问序 get/put 重排 → removeEldestEntry 淘汰 → SequencedMap 首尾与 reversed 视图。
- 边界/异常：插入序 vs 访问序两种模式；reversed 视图顺序。
- 健壮性：确定性（固定键值，无随机/墙钟）；无全局副作用；无文件/网络；跨平台。headless-safe。
