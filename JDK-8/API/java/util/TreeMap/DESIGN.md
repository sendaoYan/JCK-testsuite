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
