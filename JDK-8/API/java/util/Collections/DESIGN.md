# java.util.Collections 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。sort/reverse/shuffle/rotate/swap/fill/copy/replaceAll、binarySearch、min/max
> （空集合 NSEE）、frequency/disjoint（含 null）、reverseOrder、checked* 系列（错误类型 CCE）、
> unmodifiable* 系列（**live 视图**：底层改视图反映；改视图 UOE）、empty*/singleton*/nCopies（不可变、
> UOE、singleton 恒等）、addAll(T...)/enumeration/list、indexOfSubList/lastIndexOfSubList、
> sort 边界（sort(null) NPE、sort(list, null) 自然序、nullsFirst 排序）。

## 1. API 提供了哪些功能

- **排序/查找**：`sort`（自然序 / 自定义 Comparator，`sort(list, null)` 走自然序）、`reverse`、`shuffle`、
  `rotate`、`swap`、`binarySearch`、`min/max`、`frequency`、`disjoint`、`indexOfSubList/lastIndexOfSubList`。
- **填充/复制**：`fill`、`copy`（dest 小于 src 抛 IOBE）、`replaceAll`、`addAll(T...)`、`nCopies`。
- **包装器**：`checkedList/Set/Map/Collection`（插入错误类型抛 CCE）、
  `unmodifiable*`（live 视图、改视图 UOE）、`synchronized*`、`empty*`、`singleton*`。
- **枚举**：`enumeration`、`list`、`reverseOrder`。

## 2. Java 用户如何使用

- `Collections.sort(list, comparator)` 排序 → `binarySearch` 查找 → `min/max` 极值。
- `Collections.unmodifiableList(base)` 得到只读 live 视图；`Collections.checkedList(list, String.class)`
  在编译期外再兜底类型检查。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Collections.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Collections.java`
  （`sort(list, null)` 委托 `list.sort(null)` 走自然序；`copy` dest 不足抛 IOBE；`min/max` 空集合抛 NSEE；
  checked* 通过类型检查包装器抛 CCE）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/Collections`。

## 4. 测试用例设计（映射到 CollectionsTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 既有：sort/reverse/binarySearch/min-max/frequency/nCopies/reverseOrder/unmodifiable/empty/singleton/synchronized | testSortReverse…testSynchronized |
| P0 | sort 边界 + min/max 空集合 | testSortEdgeCases |
| P0 | copy/fill/replaceAll/rotate/swap | testCopyFillReplaceAllRotateSwap |
| P0 | addAll(T...)/enumeration/list | testAddAllEnumerationList |
| P0 | checked* 类型检查 CCE | testCheckedCollections |
| P0 | unmodifiable live 视图 + UOE | testUnmodifiableIsLiveView |
| P0 | empty*/singleton*/nCopies 不可变 | testEmptySingletonIdentity + testSingletonEdge |
| P0 | frequency/disjoint 含 null | testFrequencyDisjointNull |
| P0 | indexOfSubList/lastIndexOfSubList | testIndexOfSubList |

## 关键规范细节（易错，已锁入断言）

- **`sort(list, null)` 不抛 NPE**：按 `List.sort(null)` 走自然序（元素须 Comparable）；只有 `sort(null list)`
  抛 NPE。
- `min/max` 空集合抛 **NoSuchElementException**。
- `copy(dest, src)` 当 dest 元素数少于 src 抛 **IndexOutOfBoundsException**（不是越界覆盖）。
- `unmodifiable*` 是 **live 视图**：底层集合修改会反映到视图；对视图写操作抛 UOE。
- `emptyList()/emptySet()/emptyMap()` 各自是**单例**（`==` 恒等）；`singletonList(null)`/`nCopies(n, null)`
  允许 null 元素；对它们写抛 UOE。
- `checkedList/Set/Map/Collection`：插入元素类型与声明不符抛 **ClassCastException**（在 add/put 时立即检查）。
- `frequency(list, null)` 与 `disjoint` 均正确处理 null 元素（equals 语义）。
- `indexOfSubList`/`lastIndexOfSubList`：找不到返回 -1；返回最早/最晚出现的起始下标。

## 注意事项

- 无 GC/图形/网络/文件依赖；`shuffle` 只断言元素集合不变（不锁定随机顺序）。

## 第 2 轮深化（2026-11，契约覆盖，63→302 断言）

新增方法：testBinarySearchInsertionFormula、testRotateBoundaries、testNCopiesContract、
testEmptyIteratorContract、testEmptySortedNavigable、testSingletonContracts、
testUnmodifiableSortedNavigable、testSynchronizedFamilies、testReverseOrderContract、
testShuffleDeterminism、testMinMaxNullArgs、testDisjointNullArgs、testNewSetFromMapContract、
testAsLifoQueueContract、testEnumerationListEdges、testSerializationRoundTrip、
testCheckedMoreWrappers、testMutatorNullArgs。

### 本轮锁定的易错规范细节（先探测后断言，jdk8u372 实测）

- **`binarySearch` 插入公式**：未命中返回 `-(insertionPoint)-1`；命中返回下标。边界（小于全部/大于全部/中间）逐一锁定。
- **`rotate` 折叠**：`rotate(l, k)` 等价 `rotate(l, k mod size)`；k=0/±size 为恒等；size=1 恒等；`rotate(l, n)` == `reverse(l)`；`rotate(l, -k)` == `rotate(l, size-k)`。
- **`nCopies`**：`nCopies(-1, x)` 抛 IAE（消息 `List length = -1`）；`nCopies(0, null)` 合法且空；equals/hashCode 与等值 `ArrayList` 一致。
- **`emptyIterator().remove()` 抛 IllegalStateException**（不是 UOE）；`emptyListIterator().set/add` 抛 ISE；`emptyEnumeration().hasMoreElements()` 恒 false。
- **`emptyNavigableSet.pollFirst/pollLast()` 抛 UOE**（javadoc 明示 quirk）；`emptyNavigableMap.pollFirstEntry/pollLastEntry` 同样 UOE（本侧无 NavigableMap 变体，锁在 Set 侧）。
- **`singleton*` 恒等性弱化**：JDK-8 上 `emptyList() == emptyList()` 恒真（单例）；`singleton` 系每次新实例但 equals 恒等；`singletonList(null)` 合法。
- **`unmodifiableMap` 默认方法全 UOE**：`putIfAbsent/compute/computeIfAbsent/computeIfPresent/merge/remove(k,v)/replace*/replaceAll` 均拒绝；序列化往返后仍 UOE（不可变性随序列化保留）。
- **`reverseOrder(null)` 不抛 NPE**（javadoc 声称 IAE 但实现回退自然逆序）——按实测行为断言。
- **`min/max(list, null comparator)` 不抛 NPE**：实现静默替换为自然序 → 断言 min==最小/max==最大。
- **`disjoint(x, null)` 任意组合（含空集合）抛 NPE**：`isEmpty()` 短路仍会解引用 c1。
- **`shuffle(list, null rnd)`**：size≤1 或空列表**不抛**（rnd 未被咨询）；size≥2 抛 NPE。
- **`newSetFromMap`**：非空 map → IAE；null → NPE；map↔set 双向 live 联动。
- **`asLifoQueue(null)` 不抛**（JDK-8；与 JDK-27 相反——27 抛 NPE）。
- **序列化往返**：unmodifiableList/CheckedList 内容与不可变性均保留。

## 注意事项（第 2 轮）

- 线程 smoke：`synchronizedList` 2 线程 × 1000 次 add，`CountDownLatch` 有界 await（≤5s）；固定输入。
