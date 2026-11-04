# java.util.WeakHashMap 测试方案

## 1. API 功能（按方法分组）

- 构造器：`WeakHashMap()`、`WeakHashMap(int initialCapacity)`、`WeakHashMap(int, float)`、`WeakHashMap(Map)`。
- 核心操作：`put`/`get`/`remove`/`clear`/`putAll`。
- 查询：`size`/`isEmpty`/`containsKey`/`containsValue`。
- 视图：`keySet`/`values`/`entrySet`。
- 继承默认方法：`getOrDefault`/`putIfAbsent`/`compute*`/`merge`/`replaceAll`/`forEach`。

## 2. Java 用户如何使用

- 构造：`new WeakHashMap<>()`，用于“键可被 GC 回收”的映射（如缓存/规范化表）。
- 典型调用：`put(key, value)` 后当 key 无外部强引用时，条目可能被 GC 回收；`get/containsKey` 与普通 Map 一致。
- 真实组合链路：
  1. put 多键 → get/containsKey/containsValue/size。
  2. 视图 remove 联动原 map。
  3. null key/value 语义（允许）。
  4. equals/hashCode（顺序无关）。
- 注意：弱引用回收时机不确定，不应对 `System.gc()` 后的 `get()` 结果做确定性断言。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 测试 `test/jdk/java/util/ArrayList/AddAll.java:41-80`：多次 `new WeakHashMap(100000)` 做大批量 addAll 压力/内存相关测试，
  说明 WeakHashMap 常用于“键可回收”的大集合场景。

## 4. 测试用例设计 + 自动化实现

实现位置：`WeakHashMapTest.java`（`testWhiteBox()` + `testBasicContract()`/`testNullSemantics()`/`testViewsAndEquals()`）。

- 正常：put/get/remove/contains 基本契约。
- 组合：视图 remove 联动 + equals/hashCode。
- 边界：null key/value 允许。
- 健壮性：刻意**不**断言 `System.gc()` 后的 `get()==null`（避免不稳定）；无墙钟/随机/全局副作用；无文件/网络；跨平台。headless-safe。

## 深化追加（契约级，第二轮，JDK-27）

新增 testComputeMergeMatrix / testIterationAndCme / testEqualsHashMap：

- **compute-merge 全分支矩阵**：putIfAbsent/remove(k,v)/replace 两形态/getOrDefault/
  computeIfAbsent（absent 插入/present 不调函数/返回 null 不落 entry）/computeIfPresent
  （absent 不调函数/返回 null 删）/compute/merge（absent 放 value 不调函数/present remap/
  返回 null 删）全分支 + **异常同实例透传且条目不变** + null fn/null merge value NPE。
- **迭代与 fail-fast**：迭代内容恰好等于键集合（无序比较）；entrySet/keySet/values 迭代器
  remove 透传；迭代中 put → CME；iterator.remove 后继续迭代合法。
- **跨实现 equals**：与 HashMap 同内容互相 equals 且 hashCode 相等（WeakHashMap key 用
  equals 语义，与 IdentityHashMap 相反）；含 null key 的内容同样相等。
- GC 依赖行为仍按第一轮约定不断言。
