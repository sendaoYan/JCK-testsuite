# java.util.TreeMap 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「方法被调用一次」的冒烟。构造器矩阵、null 歧义、NavigableMap
> 边界、范围视图活对象写通道、fail-fast、默认方法深分支、toArray 契约、跨实现 equals、clone/序列化、
> 状态不变量全部补齐。断言不依赖迭代顺序（TreeMap 有序，但跨实现 equals 场景排序无关），headless 安全。

## 1. API 提供了哪些功能

- **构造器（4）**：`TreeMap()`、`TreeMap(Comparator)`、`TreeMap(Map)`、`TreeMap(SortedMap)`。
- **核心操作**：`put`、`get`、`remove`、`putAll`、`clear`、`containsKey`、`containsValue`、`size`。
- **NavigableMap 查询**：`firstKey/lastKey`、`firstEntry/lastEntry`、`lowerKey/lowerEntry`、
  `floorKey/floorEntry`、`ceilingKey/ceilingEntry`、`higherKey/higherEntry`、`pollFirstEntry/pollLastEntry`。
- **范围视图**：`subMap/headMap/tailMap`（含 boolean 含边界重载）、`descendingMap`、
  `descendingKeySet`、`navigableKeySet`；`keySet/values/entrySet`。
- **Java 8 默认方法**：`getOrDefault`、`putIfAbsent`、`replace`、`replace(K,old,new)`、
  `replaceAll`、`computeIfAbsent`、`computeIfPresent`、`compute`、`merge`、`forEach`。
- **契约继承**：`clone()`（浅拷贝）、`Serializable`（`serialVersionUID == 919286545866124006L`）、
  `Map.equals/hashCode`（内容相等，与 comparator 无关）。

## 2. Java 用户如何使用

- `new TreeMap<>()` 自然序建表；`new TreeMap<>(Comparator)` 自定义序；`new TreeMap<>(otherMap)` 拷贝排序。
- `put` 乱序插入 → `firstKey/lastKey/ceilingKey/floorKey/higherKey/lowerKey` 上下界查询。
- `subMap/headMap/tailMap` 当活视图直接改（删除/放元素，越界抛 IAE），改动联动到原 map。
- `descendingMap/descendingKeySet/navigableKeySet` 倒序/可导航视图。
- 依赖「null key 自然序/普通比较器抛 NPE、null value 合法」；默认方法做批量计算。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/TreeMap.html`：NavigableMap 契约、范围视图 write-through、
  null key NPE、`Map.Entry` 快照 setValue UOE、clone 浅拷贝、序列化。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/TreeMap`（`NullAtAndBelow` 7177585、
  `HeadTailTypeError` 8006469、`SubMap` 等）：空表首尾、范围视图类型错误、子视图一致性。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/TreeMap`：
  构造器等价类、边界值表、范围视图伞形套件、序列化 round-trip。

## 4. 测试用例设计（映射到 TreeMapTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器矩阵：null Map/SortedMap→NPE、null Comparator 等价自然序、TreeMap(Map) 含 null key→NPE、TreeMap(SortedMap) 保留 comparator、null-tolerant comparator 允许 null key | testConstructorMatrix |
| P0 | null 语义：natural/comparator 下 put/get/containsKey/remove/computeIfAbsent(null) → NPE；null value 合法；containsValue(null) | testNullSemantics |
| P0 | NavigableMap 边界：空表 firstKey/lastKey NSEE、first/last/poll entry null、lower/floor/ceiling/higher 越界 null | testNavigableBoundaries |
| P0 | 范围视图写通道：subMap put 越界 IAE、put/remove/clear 联动、from>to IAE、exclusive 边界拒放；headMap/tailMap 越界 IAE | testRangeViewWriteThrough |
| P0 | descendingMap/descendingKeySet/navigableKeySet 活视图：put/remove/pollFirst/pollLast 联动、反向 comparator | testDescendingNavigableViews |
| P0 | keySet/values/entrySet 写通道：add→UOE、remove/removeAll/clear 联动、entrySet iterator.remove | testViewWriteChannels |
| P0 | Entry.setValue 返回旧值并写回；firstEntry/lastEntry/lowerEntry 快照 setValue→UOE | testEntrySetValue |
| P0 | fail-fast：put/remove 后 keySet/entrySet 迭代 CME；同 key 覆盖不 CME | testFailFast |
| P0 | 默认方法深分支：putIfAbsent/getOrDefault/replace/replaceAll、compute 家族 side-effect 计数器、函数返 null 删/不插入、异常透传且状态不变、merge null 结果删除 | testDefaultMethods |
| P0 | forEach 升序 + forEach(null) NPE | testForEachOrdering |
| P0 | 跨实现 equals：TreeMap vs HashMap/LinkedHashMap 对称 + hashCode；不同 comparator 的 TreeMap 内容相等 | testCrossImplEquals |
| P1 | toArray 契约：数组复用 ==、null 终止符、精确长度、keySet/entrySet 升序 | testToArrayContract |
| P1 | clone 浅拷贝 + 结构独立 + comparator 保留 | testClone |
| P1 | 序列化 round-trip（含 null value）+ serialVersionUID | testSerialization |
| P1 | 状态不变量：删除/失败操作后其余映射完整、clear 后逐 key 验证 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- `TreeMap(Map)` 自然序时若源 map 含 null key → NPE；`TreeMap(SortedMap)` 保留源 comparator。
- `firstEntry/lastEntry/lowerEntry/...` 返回**不可变快照**，`setValue` 抛 UOE；`entrySet().iterator().next()`
  返回**活 entry**，`setValue` 写回 map。
- 范围视图 `put` 越界抛 IAE；`subMap(from,to)` from>to 抛 IAE；exclusive 边界同样拒放边界元素。
- 同 key 覆盖（put/replace/setValue）不是结构修改，不触发 CME。
- null key 是否合法取决于 comparator：自然序/普通 comparator 抛 NPE，null-tolerant comparator（如
  `nullsFirst`）允许；null value 始终合法。

## 注意事项

- TreeMap 迭代有序（升序/降序），故可断言顺序；跨实现 equals 断言只依赖内容相等。
- 序列化断言只锁定官方稳定 serialVersionUID（JDK-8 与 JDK-27 相同）。
- 不碰图形/网络/文件/全局状态；无 GC 依赖（本类无弱引用回收测试点）。


## 5. 契约级深化追加（2026-11，115→245 断言）

新增方法：testSerializationWithComparator / testIteratorRemovePattern / testSubMapParentCMEAndBounds /
testPresentNullValueBranches / testNearestFullMatrix / testInclusiveBoundEndpoints /
testValuesOrderAndDescendingMirror / testNullTolerantComparatorOps。

| 维度 | 锁定内容 |
|---|---|
| comparator 序列化 | String.CASE_INSENSITIVE_ORDER round-trip 保序且 readResolve 复原共享实例（comparator()==同一实例）；reverseOrder 同；**不可序列化 Comparator → ObjectOutputStream 抛 NotSerializableException**（整图不可写） |
| 迭代器 remove 模式 | 未 next 先 remove ISE；next/remove 交替按升序排干；连续两次 remove 第二次 ISE；自身 remove 后继续 next 合法（不 CME） |
| subMap 视图细节 | 视图迭代器 remove 删父图键；父结构 put → 视图迭代器 CME；界值 put IAE（[3,8) 拒 8 与 2）；范围内 put 落父图；父删反映视图 size |
| present-null-value 全分支 | putIfAbsent 覆盖 null 返 null；getOrDefault 返 null 非 default；computeIfAbsent **调用**函数（计数器）；computeIfPresent 忽略；compute 见 null v 且 null 结果删 entry；merge 跳过函数直取 value；remove(k,null) 删；replace(k,null,v) true；异常 relays 同一对象且映射不变 |
| 四方法 28 格矩阵 | keys {10,20,30} × probes {5..35}：lower/floor/ceiling/higher 精确（entry 形态键值双验） |
| inclusive 界点 | headMap/tailMap/subMap × (true,false) firstKey/lastKey/size 精确；不存在界值按比较语义（tailMap(15) 空、headMap(15) 止于 10） |
| 迭代序/降序镜像 | values 与 toString 键序；desc 四方法翻转（floor(4)=5/ceiling(4)=3）；desc.descendingMap().equals(原)；desc.keySet=[5,3,1] |
| null 容忍 comparator | nullsFirst/nullsLast 下 null key 合法且排首/尾；containsKey/get/remove(null) 可用；higher/lower(null probe) 工作 |

### 易错规范细节（实测锁定）

- `CASE_INSENSITIVE_ORDER` 下 put("BANANA") 会**覆盖** "banana"（同一 entry），get 两者都返回新值。
- filled(10) 上 `tailMap(15,true)` 为空——firstKey 抛 NSEE（不存在 >15 的键）；不存在界值不制造键。
- present 键 computeIfAbsent(5, throwing) **不抛**（函数被跳过）——异常透传要用 absent 键验证。
- 视图 size 是惰性按范围计算的：子图先前删过键后 size 断言要按当前内容算。
