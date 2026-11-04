# java.util.Spliterator 测试设计

## 1. API 功能
- 遍历：`tryAdvance`、`forEachRemaining`。
- 拆分：`trySplit`、`estimateSize`、`getExactSizeIfKnown`。
- 特征：`characteristics`、`hasCharacteristics`、`getComparator`。

## 2. Java 用户如何使用
- 从集合获取：`list.spliterator()`。
- 手动遍历：`while (sp.tryAdvance(consumer)) { }`。
- 并行拆分：`sp.trySplit()`。
- 判断特征：`sp.hasCharacteristics(Spliterator.ORDERED)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/lang/Iterable.java`：`spliterator()` 默认实现。
- JDK 源码 `java/util/ImmutableCollections.java`：自定义 spliterator。
- JDK 源码 `java/util/IdentityHashMap.java`：`spliterator()` 实现。
- 广泛用法：流框架的并行遍历基础。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言接口与关键方法存在。
- 黑盒（ArrayList 的 spliterator，确定有序/有大小）：
  - estimateSize/getExactSizeIfKnown、ORDERED/SIZED 特征。
  - tryAdvance 遍历顺序、trySplit 保持总大小、forEachRemaining。
  - 非 SORTED 时 getComparator 抛 IllegalStateException。

## 契约深化（第二轮，追加）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 8 个特征常量精确值（javap -constants 双侧锁定，JDK-8 与 27 完全一致） | testConstantValues |
| P0 | ArrayList/HashSet/LinkedHashSet/TreeSet 特性位精确值（16464/65/16465/85） | testImplCharacteristics |
| P0 | SORTED 下 getComparator == 构造比较器实例 / 自然序 null | testSortedGetComparator |
| P0 | trySplit 前半/后半精确内容（mid=size>>>1）+ 8 元素对半 | testTrySplitHalves |
| P0 | 默认方法分派：forEachRemaining 循环调 tryAdvance（计数器）；getExactSizeIfKnown 非 SIZED 返 -1 | testDefaultMethodDelegation |
| P1 | 非 SIZED spliterator estimateSize==Long.MAX_VALUE | testUnsizedEstimates |

### 易错规范细节（实测锁定）

- **TreeSet 特性位 = 85 = ORDERED|DISTINCT|SORTED|SIZED**（有序天然含 ORDERED）。
- ArrayList trySplit 返回前缀、原实例变后缀，切分点 `mid = (lo+hi)>>>1`。
- 耗尽后的 forEachRemaining 是 no-op；hasCharacteristics(0) 恒 true。
- 特性位与常量值与 JDK-8 **完全一致**（无版本差异）。
