# java.util.Collections 测试方案

对应测试：`CollectionsTest.java`（`testWhiteBox()` 结构断言 + 后续 `test*` 黑盒行为断言）。

## 1. API 功能（按方法分组）

- 算法：`sort`、`binarySearch`、`reverse`、`shuffle`、`rotate`、`swap`、`fill`、`copy`、
  `replaceAll`、`min`、`max`、`frequency`、`disjoint`、`addAll`、`indexOfSubList`、`lastIndexOfSubList`
- 比较器：`reverseOrder()`、`reverseOrder(Comparator)`
- 包装器：`unmodifiableList/Set/Map/Collection/…`、`synchronizedList/Set/Map/…`、
  `checkedList/Set/Map/Collection/…`
- 工厂：`emptyList/emptySet/emptyMap`、`singleton/singletonList/singletonMap`、`nCopies`
- 空迭代器：`emptyIterator()`、`emptyListIterator()`、`emptyEnumeration()`
- 视图/其它：`enumeration(Collection)`、`list(Enumeration)`、`newSetFromMap(Map)`、`asLifoQueue(Deque)`

## 2. Java 用户如何使用

```java
List<Integer> list = new ArrayList<>(Arrays.asList(3, 1, 2));
Collections.sort(list);                 // 原地排序
int i = Collections.binarySearch(list, 2);
Collections.reverse(list); Collections.shuffle(list, new Random(42)); // 固定种子可复现
Collections.rotate(list, 2); Collections.swap(list, 0, 1); Collections.fill(list, 0);
int n = Collections.frequency(list, 1);
List<String> unmod = Collections.unmodifiableList(base);   // 只读包装
List<String> one = Collections.singletonList("x");
```

## 3. 开源代码实际用法调研

来源：`~/git/jdk25u-dev`（JDK 源码）；网络检索未执行，以下为 JDK 自用、逐条标注。

- `java/io/DeleteOnExitHook.java:79` — `Collections.reverse(toBeDeleted);`（算法）
- `java/net/URLPermission.java:228-233` — `Collections.sort(l); this.methods = Collections.unmodifiableList(l);`（排序后只读包装）
- `java/net/CookieManager.java:283` — `cookies = java.util.Collections.emptyList();`（空列表工厂）
- `java/lang/reflect/Proxy.java:568` — `this(loader, Collections.singletonList(intf));`（单元素列表）
- `java/util/ReverseOrderSortedSetView.java:43` — `comp = Collections.reverseOrder(set.comparator());`（比较器）

结论：Collections 算法 + 包装器在 JDK 中被广泛用于“排序后不可变发布”“空/单元素工厂”“只读视图”。

## 4. 测试用例设计 + 自动化实现

- 正常：sort/binarySearch（自然序 + Comparator）；reverse/rotate/swap/fill/copy/replaceAll；
  min/max/frequency/disjoint/nCopies/addAll/indexOfSubList/lastIndexOfSubList；reverseOrder。
- 组合链路：`sort → binarySearch → reverse/rotate/shuffle(种子)/swap/fill/copy → checkedList/
  unmodifiableList → singleton/empty 工厂 → asLifoQueue/newSetFromMap`。
- 边界/异常：`copy` 目标过小 → `IndexOutOfBoundsException`；`min/max` 空集合 →
  `NoSuchElementException`；`checkedList` 插入错类型 → `ClassCastException`；unmodifiable/singleton/
  empty 的写操作 → `UnsupportedOperationException`。

健壮性：`shuffle` 用固定 `Random(42)` 种子断言可复现（无无种子随机）；集合比较先排序（不依赖 HashMap/
HashSet 迭代顺序）；固定输入、无墙钟；无全局副作用；headless，无 `@key`。

## 第 2 轮深化（2026-11，契约覆盖，107→308 断言）

新增方法：testBinarySearchInsertionFormula、testRotateNCopiesBoundaries、
testEmptyIteratorsEnumerations、testSingletonHashCodeFormulas、testUnmodifiableWriteChannels、
testSynchronizedConcurrentSmoke、testReverseOrderAndShuffle、testMinMaxDisjointNulls、
testNewSetFromMapLifoQueue、testEnumerationSerializationRoundTrip。

### 本轮锁定的易错规范细节（先探测后断言，jdk-27.0.1 实测）

- **`binarySearch` 插入公式**：未命中返回 `-(insertionPoint)-1`，边界（首/中/尾/越两端）逐一锁定。
- **`rotate` 折叠**：`k mod size` 等价；k=0 恒等；`rotate(l, n)` == `reverse(l)`。
- **`nCopies(-1, x)` IAE**（消息 `List length = -1`）；`nCopies(0, null)` 合法；hashCode 与公式 `1*31^n + ...` 一致。
- **`emptyIterator().remove()` 抛 IllegalStateException**（非 UOE）；`emptyListIterator().set` ISE；`emptyNavigableMap.pollFirstEntry/pollLastEntry`、`emptyNavigableSet.pollFirst/pollLast` 抛 **UOE**（javadoc 明示）。
- **`unmodifiable*` 默认方法写通道全 UOE**：putIfAbsent/compute*/merge/remove(k,v)/replace*/replaceAll。
- **`reverseOrder(null)` 不抛 NPE**（javadoc 声称 IAE，实现回退自然逆序）——按实测断言。
- **`min/max(coll, null)` 静默走自然序**（非 NPE）。
- **`disjoint(*, null)` 全组合 NPE**（含空集合：isEmpty() 短路仍解引用）。
- **`asLifoQueue(null)` 抛 NPE（JDK-27）**——与 JDK-8 相反（8 不抛），版本差异已双侧各自锁定。
- **`shuffle(List, RandomGenerator)`（JDK-17+）与 `shuffle(List, Random)` 同种子结果一致**：`new Random(42)` 双 API 等价断言。
- **`newSetFromMap`**：非空 map IAE、null NPE、双向 live 联动（map 侧改动反映到 set）。
- **序列化往返**：unmodifiableList 反序列化后仍 UOE、内容不变；CheckedList 类型检查保留。

## 第 2 轮健壮性

- synchronized 并发 smoke：2 线程 × 1000 add → 2000，`CountDownLatch` await ≤5s；固定输入无 sleep。
