# java.util.Spliterators 测试设计

## 1. API 功能
- 数组/基本类型数组：`spliterator(Object[]/int[]/long[]/double[], ...)`。
- 集合/迭代器：`spliterator(Collection, int)`、`spliterator(Iterator, size, int)`、`spliteratorUnknownSize(Iterator, int)`。
- 空：`emptySpliterator`、`emptyInt/Long/DoubleSpliterator`。
- 转换：`iterator(Spliterator)`。

## 2. Java 用户如何使用
- 自定义源转 spliterator：`Spliterators.spliterator(array, 0)`。
- 未知大小迭代器：`Spliterators.spliteratorUnknownSize(it, characteristics)`。
- 空流：`Spliterators.emptySpliterator()`。
- 反向转换：`Spliterators.iterator(spliterator)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/Arrays.java`：数组 spliterator 委托 `Spliterators.spliterator`。
- JDK 源码 `java/util/ArrayList.java`：`spliterator()` 使用 `Spliterators`。
- JDK 源码 `java/util/stream/StreamSupport.java`：把 spliterator 转流。
- 广泛用法：自定义集合/数据源接入流框架。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言 final 类、关键静态方法存在。
- 黑盒：
  - 数组 spliterator 大小与遍历、空 spliterator。
  - 集合 spliterator 大小与特征。
  - unknownSize 的 estimateSize==Long.MAX_VALUE 与遍历。
  - `iterator(spliterator)` 转换与耗尽。

## 契约深化（第二轮，追加）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | ranged 大小/特征 16464/from==to 空 | testArrayRanged |
| P0 | ranged 越界矩阵 AIOOBE + null 数组/集合 NPE | testArrayRangedErrors |
| P0 | int/long/double 原生数组工厂内容 | testPrimitiveArraySpliterators |
| P0 | empty* 家族特征 16448、tryAdvance false | testEmptyPrimitiveSpliterators |
| P0 | OfInt 适配工厂内容 + unknownSize est=Long.MAX_VALUE | testPrimitiveIteratorSpliterators |
| P0 | iterator 桥接 remove→UOE、耗尽 next→NSEE | testIteratorBridge |
| P0 | unknownSize/集合 trySplit SUBSIZED 尺寸守恒 | testCollectionAndUnknownSize |

### 易错规范细节（实测锁定）

- `from > to` 抛 **ArrayIndexOutOfBoundsException**（非 IAE）；null 源 → NPE（裸 null 需 cast 消歧义）。
- unknownSize estimateSize == **Long.MAX_VALUE**；可用 trySplit 时两侧 SUBSIZED 且尺寸相加守恒。
- `iterator(sp)` 桥接不支持 remove（UOE）；耗尽后 next 抛 NSEE。
- 与 JDK-8 的 API 面完全一致（无 27 新方法差异），断言可双侧复用。
