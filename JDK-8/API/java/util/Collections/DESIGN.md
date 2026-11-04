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
