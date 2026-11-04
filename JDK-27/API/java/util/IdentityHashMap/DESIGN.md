# java.util.IdentityHashMap 测试方案

## 1. API 功能（按方法分组）

- 构造器：`IdentityHashMap()`、`IdentityHashMap(int expectedMaxSize)`、`IdentityHashMap(Map)`。
- 核心操作：`put`/`get`/`remove`/`remove(K,V)`/`clear`。
- 查询：`size`/`isEmpty`/`containsKey`/`containsValue`。
- 视图：`keySet`/`values`/`entrySet`。
- 契约方法：`equals`/`hashCode`（按引用相等语义）。

## 2. Java 用户如何使用

- 构造：`new IdentityHashMap<>()`，用于需要**引用相等（==）**而非 `equals` 的场景（如跟踪对象身份、序列化对象替换、按身份去重）。
- 典型调用：`put(k1, v)` 后 `get(k1)` 按引用命中；`get(new String("x"))` 与已存 `new String("x")` 不同。
- 真实组合链路：
  1. 放入两个内容相等但引用不同的 key → 两条独立映射。
  2. get/containsKey/remove 按引用命中；containsValue/remove(K,V) 也按引用比较 value。
  3. null key 允许；视图联动；equals 按引用语义比较 key 与 value。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 测试 `test/jdk/java/util/concurrent/PriorityBlockingQueue/WhiteBox.java:134/175`：
  `Collections.newSetFromMap(new IdentityHashMap())` —— 用 IdentityHashMap 作“按身份相等”的 Set 底层。
- JDK 测试 `test/jdk/java/util/concurrent/ConcurrentHashMap/MapCheck.java:281`：
  `IdentityHashMap seen = new IdentityHashMap(size)` —— 用身份集合追踪已见对象。

## 4. 测试用例设计 + 自动化实现

实现位置：`IdentityHashMapTest.java`（`testWhiteBox()` + `testKeyIdentity()`/`testValueIdentity()`/`testNullKeyAndViews()`/`testEqualsUsesIdentity()`）。

- 正常：key 引用相等语义。
- 组合：value 引用相等（containsValue/remove(K,V)）、null key、视图联动。
- 边界/异常：内容相等但引用不同视为不同条目；equals 也按引用。
- 健壮性：确定性（固定 new String 对象）；无全局副作用；无文件/网络；跨平台。headless-safe。

## 深化追加（契约级，第二轮，JDK-27）

新增 testRemoveReplaceValueSemantics / testComputeMergeIdentity / testEqualsVsHashMap /
testViewsAndFailFast：

- **remove(K,V)/replace(K,V,V) 语义（版本差异核心）**：JDK-8 未覆写，走 Map 默认实现按
  **equals** 比较现值 → equal-but-not-identical value 的 remove(k,v)/replace(k,old,new) 均
  成功；JDK-21+ 显式覆写为 **== 同一性**比较 → 均失败（JDK-27 侧断言行为 + 反射锁定
  getDeclaredMethod 存在）。JDK-8 侧文件记录 equals 语义为版本差异。
- **compute-merge 同一性矩阵**：同引用 key 的 present 分支不调函数；equal-but-distinct key
  被==判为 absent → computeIfAbsent/putIfAbsent 插入**第二条**映射（size 2）；compute/
  computeIfPresent/merge 同引用分支全断言；异常同实例透传；null key 正常参与同一性语义。
- **与 HashMap 的 equals 契约背离（javadoc 明示故意违反 Map 对称契约）**：
  IdentityHashMap.equals(HashMap)==false；而 HashMap.equals(IdentityHashMap) 在 key 为**同一
  引用**时为 true（HashMap 用自己的 entry 逐个到对方 get 查找）→ 断言该不对称性；
  key 引用不同时不相等。
- **fail-fast + 写通道**：迭代器结构修改 → CME；keySet/values 迭代器 remove 透传；
  entry.setValue 反写；getOrDefault 同一性查找；单条目 toString "{k=1}"；keySet.add UOE。
