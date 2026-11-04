# java.util.Spliterators 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。数组/基本类型数组/集合/迭代器各工厂的大小与特征位、
> 空 spliterator 家族、ranged 版本与越界异常（AIOOBE/NPE）、iterator 桥接（remove UOE、
> 耗尽 NSEE）、unknownSize 的 Long.MAX_VALUE 估计。

## 1. API 提供了哪些功能

- **数组工厂**：`spliterator(Object[], int)`、`spliterator(Object[], from, to, int)`、
  `spliterator(int[]/long[]/double[], ...)`、`spliterator(PrimitiveIterator.OfX, size, int)`。
- **集合/迭代器**：`spliterator(Collection, int)`、`spliterator(Iterator, size, int)`、
  `spliteratorUnknownSize(Iterator, int)`。
- **空**：`emptySpliterator`、`emptyIntSpliterator`、`emptyLongSpliterator`、`emptyDoubleSpliterator`。
- **反向转换**：`iterator(Spliterator)` → Iterator。

## 2. Java 用户如何使用

- 把数组/迭代器接入 Stream 框架：`StreamSupport.stream(Spliterators.spliterator(arr, 0), false)`。
- 未知大小的游标源：`spliteratorUnknownSize(it, ORDERED)`。
- 旧代码桥接：`iterator(sp)` 转回 Iterator 逐个消费。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Spliterators.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Spliterators.java`。

## 4. 测试用例设计（映射到 SpliteratorsTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 数组全量/ ranged 大小、特征位 16464、from==to 空 | testArrayRanged |
| P0 | ranged 越界矩阵：from>to、from<0、to>len → ArrayIndexOutOfBoundsException；null 数组/集合 → NPE | testArrayRangedErrors |
| P0 | int/long/double 原生数组工厂内容+大小 | testPrimitiveArraySpliterators |
| P0 | empty* 家族：tryAdvance false、特征 16448、trySplit null | testEmptyPrimitiveSpliterators |
| P0 | PrimitiveIterator.OfX 适配工厂内容 | testPrimitiveIteratorSpliterators |
| P0 | unknownSize estimateSize==Long.MAX_VALUE；trySplit SUBSIZED 尺寸守恒 | testCollectionAndUnknownSize |
| P0 | iterator(sp) 桥接：remove → UOE、耗尽后 next → NSEE | testIteratorBridge |

## 关键规范细节（易错，已锁入断言）

- ranged 数组工厂 `from > to` 抛 **ArrayIndexOutOfBoundsException**（不是 IAE）。
- `spliterator((Object[])null, 0)` / `spliterator((Collection)null, 0)` → **NullPointerException**；
  裸 `spliterator(null, 0)` 会歧义编译错，必须 cast。
- unknownSize 传入特征会被强制补 **SIZED 之外**的语义：estimateSize 返 **Long.MAX_VALUE**；
  其 trySplit 若可用则两侧 SUBSIZED 且尺寸相加守恒。
- `iterator(sp)` 返回的 Iterator 不支持 remove（**UOE**），耗尽后 next 抛 **NSEE**。
- JDK-8 无 `asIterator`/9+ 方法（Enumeration 侧同理），不测。

## 注意事项

- 空原生 spliterator 特征位 **16448 = SIZED\|SUBSIZED**，与 emptySpliterator() 相同。
