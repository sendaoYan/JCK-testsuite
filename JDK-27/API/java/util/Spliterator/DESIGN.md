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
