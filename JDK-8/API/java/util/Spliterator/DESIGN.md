# java.util.Spliterator 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。estimateSize/getExactSizeIfKnown、characteristics/hasCharacteristics、
> tryAdvance（耗尽返回 false）、forEachRemaining、trySplit 保持总量、非 SORTED 上 getComparator
> 抛 IllegalStateException。

## 1. API 提供了哪些功能

- **度量**：`estimateSize`（估计大小）、`getExactSizeIfKnown`（SIZED 时精确，否则 -1）。
- **特征**：`characteristics`、`hasCharacteristics`（ORDERED/SIZED/SORTED/...）。
- **遍历**：`tryAdvance`（单元素，耗尽返回 false）、`forEachRemaining`（消费剩余）。
- **切分**：`trySplit`（返回前缀，保持总量）。
- **排序**：`getComparator`（仅 SORTED 可用，否则 ISE）。

## 2. Java 用户如何使用

- `collection.spliterator()` 并行切分；`hasCharacteristics(SIZED)` 判断是否精确；
  `tryAdvance`/`forEachRemaining` 消费。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Spliterator.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/ArrayList.java` /
  `HashSet.java`（Spliterator 特征差异）。

## 4. 测试用例设计（映射到 SpliteratorTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | SIZED 度量 + 特征 + tryAdvance 耗尽 | testSized |
| P0 | trySplit 保持总量 | testSplit |
| P0 | forEachRemaining + getComparator ISE | testForEachRemainingAndComparator |
| P0 | 非 SIZED（HashSet）getExactSizeIfKnown==-1 | testUnsized |

## 关键规范细节（易错，已锁入断言）

- `getExactSizeIfKnown` 仅在 **SIZED** 时返回 estimateSize，否则返回 **-1**。
- `tryAdvance` 耗尽后返回 **false** 且不再调用 consumer。
- `getComparator` 在非 **SORTED** 上抛 **IllegalStateException**。

## 注意事项

- ArrayList spliterator 是 ORDERED|SIZED；用 `Spliterators.spliteratorUnknownSize(it, 0)`
  构造**非 SIZED** spliterator，锁 `getExactSizeIfKnown == -1`。

## 契约深化（第二轮，追加）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 8 个特征常量精确值（javap -constants 双侧锁定） | testConstantValues |
| P0 | ArrayList/HashSet/LinkedHashSet/TreeSet 特性位精确值（16464/65/16465/85） | testImplCharacteristics |
| P0 | SORTED 下 getComparator == 构造比较器实例 / 自然序 null | testSortedGetComparator |
| P0 | trySplit 前半/后半精确内容（mid=size>>>1）+ 8 元素对半 | testTrySplitHalves |
| P0 | 默认方法分派：forEachRemaining 循环调 tryAdvance（计数器）；getExactSizeIfKnown 非 SIZED 返 -1 | testDefaultMethodDelegation |
| P1 | 非 SIZED spliterator estimateSize==Long.MAX_VALUE | testUnsizedEstimates |

### 易错规范细节（实测锁定）

- **TreeSet 特性位 = 85 = ORDERED|DISTINCT|SORTED|SIZED**（有序天然含 ORDERED；只写 DISTINCT|SORTED|SIZED=69 是错的）。
- ArrayList trySplit 契约：返回**前缀**、原实例变**后缀**，切分点 `mid = (lo+hi)>>>1`：5 元素切 [a,b]/[c,d,e]，8 元素切 4/4。
- 耗尽后的 forEachRemaining 是 **no-op**（不抛、不调 consumer）。
- hasCharacteristics(0) 恒 true。
