# java.util.Arrays 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。sort（原始类型 / Object 自然序 / Comparator / range / parallelSort）、
> binarySearch 返回值 + range 边界、equals/deepEquals（含 null 元素 / 嵌套数组）、fill、copyOf/copyOfRange
> （截断/填充/边界异常）、asList（固定大小视图 + 原始类型数组单元素 + null NPE）、toString/deepToString/
> hashCode/deepHashCode、sort 边界（null NPE、from<0/to>length AIOOBE、from>to IAE、非 Comparable CCE）、
> binarySearch 边界（range 异常、null key NPE）、copy/fill 边界异常。

## 1. API 提供了哪些功能

- **排序**：`sort`（各原始类型 / `Object[]` 自然序 / `T[]` + Comparator / range 变体）、`parallelSort`。
- **查找**：`binarySearch`（原始类型 / `Object[]` / range / 带 Comparator）。
- **比较**：`equals`、`deepEquals`（递归比较嵌套数组）、`hashCode`、`deepHashCode`。
- **复制/填充**：`copyOf`、`copyOfRange`（截断或补零/null）、`fill`（含 range）。
- **视图/字符串**：`asList`（固定大小视图）、`toString`、`deepToString`。

## 2. Java 用户如何使用

- `Arrays.sort(a)` 排序 → `Arrays.binarySearch(a, key)` 查找 → `Arrays.copyOf(a, n)` 扩容/截断 →
  `Arrays.asList(a)` 数组当 List 用（固定大小）。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Arrays.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Arrays.java`
  （`copyOfRange` 仅 from<0 或 from>length 抛 AIOOBE、from>to 抛 IAE，to>length 允许并补零；
  `sort(Object[])` 对非 Comparable 元素抛 CCE）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/Arrays`。

## 4. 测试用例设计（映射到 ArraysTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 既有：sort/binarySearch/equals-fill/copy/asList/toString-hash | testSort…testToStringHash |
| P0 | sort 边界 + 非 Comparable CCE + parallelSort range | testSortEdgeCases |
| P0 | binarySearch range 异常 + null key NPE | testBinarySearchEdgeCases |
| P0 | copyOf/copyOfRange 边界异常 + to>length 补零 | testCopyEdgeCases |
| P0 | fill range 异常 | testFillEdgeCases |
| P0 | equals/deepEquals/hashCode 含 null/嵌套/空数组 | testEqualsDeepEdge |
| P0 | asList 边界（原始类型单元素/UOE/null NPE） | testAsListEdge |
| P0 | toString/deepToString/deepHashCode 含 null/嵌套 | testToStringHashEdge |

## 关键规范细节（易错，已锁入断言）

- **`copyOfRange(a, 0, 4)` 当 length==3 不抛异常**：to>length 允许，结果补零/null 到长度 4。
  只有 `from<0`、`from>original.length` 抛 AIOOBE，`from>to` 抛 IAE。
- `copyOf(a, -1)` 抛 **NegativeArraySizeException**。
- `sort(Object[])` 元素不可比（非 Comparable）抛 **ClassCastException**；`sort(null)` 抛 NPE；
  `sort(a, from, to)` 的 from<0 或 to>length 抛 AIOOBE、from>to 抛 IAE。
- `binarySearch(Object[], null key)` 抛 **NPE**（自然序比较器不接受 null）。
- `asList(int[])`（原始类型数组）返回**单元素** `List<int[]>`（数组整体作为一个元素）。
- `asList` 视图：`set`/数组元素修改双向联动；`add/remove/clear` 抛 UOE；`asList((Object[]) null)` 抛 NPE。
- `equals(Object[])` 对 null 元素逐项比较（`null` equals `null` 为 true）；`deepEquals` 递归比较嵌套数组。
- `hashCode(new int[0]) == 1`（空数组哈希值为 1）。

## 注意事项

- 无 GC/图形/网络/文件依赖；不锁定未排序数组的 binarySearch 结果（契约未定义）。

## 第 2 轮深化（2026-11，契约覆盖，48→190 断言）

新增方法：testSortAllPrimitiveTypes、testSortStability、testBinarySearchAllTypes、
testStreamRanges、testParallelPrefixSetAll、testNullArrayContracts、testHashCodeFormula、
testCopyOfAllTypes、testFillAllTypes、testEqualsFloatingSemantics、testAsListDeepContract、
testSpliteratorCharacteristics。

### 本轮锁定的易错规范细节（先探测后断言，jdk8u372 实测）

- **null 数组契约矩阵**：`equals(null,null)=true`、`equals(null,arr)=false`、`hashCode(null)=0`、
  `toString(null)="null"`；`asList(null)`/`stream(null,0,0)` 等 NPE。
- **hashCode 公式**：`result=1; result=31*result+hash(e)`：`{1,2}`→994、`asList("a","b")`→4066 手工锁定。
- **equals 浮点语义**：`double[]` NaN==NaN true、+0.0!=-0.0（`Double.compare` 语义，非 `==`）。
- **sort 稳定性**：`sort(T[], cmp)` 相等键保持相遇顺序（TimSort 保证）；全原始类型 sort+binarySearch 一致性。
- **stream range 违规 AIOOBE**：`stream(a, 1, 0)` IAE（from>to）、`stream(a, -1, 2)` / `stream(a, 0, 6)` AIOOBE。
- **asList 深契约**：set↔数组双向联动；iterator `remove` **未 next 先调抛 IllegalStateException（JDK-8）**——与 JDK-27 相反（27 抛 UOE），版本差异双侧锁定；`listIterator.set` 在 next 后可用。
- **spliterator 特征**：`0x4450`（ORDERED|SIZED|IMMUTABLE|SUBSIZED，**无 NONNULL**）对 int[] 与 String[] 一致。

## 注意事项（第 2 轮）

- 所有断言固定输入；parallelPrefix/parallelSetAll 用确定性生成函数。
