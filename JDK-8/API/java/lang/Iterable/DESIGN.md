# java.lang.Iterable 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是"遍历一下"的冒烟。重点补：白盒结构（恰 1 抽象方法 iterator，
> forEach/spliterator 为 default）、增强 for 与 iterator() 等价、默认 forEach 语义（恰好 n 次、
> 按序、异常透传同一实例且中断遍历、forEach(null) → NPE）、iterator() 返回 null 的 for-each 负例、
> 不可变/可变迭代器 remove 双面、双迭代器独立游标、默认 spliterator（未知尺寸 Long.MAX_VALUE）、
> 空Iterable 三路径（for-each/iterator/forEach/spliterator）零调用。

## 1. API 提供了哪些功能

- **接口** `Iterable<T>`：唯一抽象方法 `Iterator<T> iterator()`；
  default `forEach(Consumer<? super T>)`（默认实现 `requireNonNull(action)` 后逐元素 accept）；
  default `spliterator()`（默认 `spliteratorUnknownSize(iterator(), 0)` → estimateSize 为
  `Long.MAX_VALUE`、getExactSizeIfKnown 为 -1）。
- 增强 for（JLS 14.14.2）对 `Iterable` 的展开：`for (Iterator i = it.iterator(); i.hasNext();) ...`
  —— iterator() 返回 null 时在 `hasNext()` 调用处抛 NPE。

## 2. Java 用户如何使用

- 实现 `iterator()` 即可获得 for-each 能力（集合框架全部实现）；
- `forEach(action)` 用于无索引遍历；action 为 null 直接 NPE；
- 自造类型不覆写 forEach/spliterator 时走接口默认实现（本批实证路径）。

```java
for (String s : myIterable) { ... }
myIterable.forEach(System.out::println);
```

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Iterable.html`）：forEach 的顺序/异常透传/NullPointerException 契约。
- **JLS §14.14.2** The enhanced for statement（Iterable 形态的编译器展开）。
- **OpenJDK 8u-dev** `java/lang/Iterable.java`（默认 forEach/spliterator 源码事实）。

## 4. 测试用例设计（映射到 IterableTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：3 公共方法、1 抽象、forEach/spliterator default、类型参数 T | testWhiteBox |
| P0 | 耗尽后 next() → NoSuchElementException | testIterator（原有） |
| P0 | 增强 for 与手工 iterator() 逐一相等 | testForEachEquivalence |
| P0 | 默认 forEach：恰好 n 次、按序 | testForEachDefaultSemantics |
| P0 | 默认 forEach：异常透传同一实例且中断遍历 | testForEachDefaultSemantics |
| P0 | forEach(null) → NullPointerException（无消息） | testForEachNullAction |
| P0 | iterator() 返回 null → for-each NPE | testNullIteratorForEnhancedFor |
| P1 | 不可变迭代器 remove → UnsupportedOperationException | testImmutableIteratorRemove |
| P1 | 可变迭代器 remove：next 前 IllegalStateException、删最近返回 | testMutableIteratorRemove |
| P1 | 双迭代器独立游标互不干扰 | testIndependentIteratorCursors |
| P1 | 空Iterable：四条路径全零调用 | testEmptyIterable |
| P1 | Iterable 可重复迭代（每次新迭代器） | testIterableIsRepeatable |
| P1 | 默认 spliterator：Long.MAX_VALUE / -1 / tryAdvance 顺序 / forEachRemaining | testSpliteratorOnCustom |
| P2 | 协变消费：`Iterable<? extends CharSequence>` | testCovariantConsumption |
| P2 | 数组/String 不是 Iterable 实例（Class 事实） | testForEachEquivalence |

## 易错规范细节

- **forEach(null) 的 NPE 无消息**（默认实现走 `Objects.requireNonNull(action)` 裸抛）——
  实测两侧一致，勿断言消息非 null。
- **for-each 的两条路径**：数组走索引、Iterable 走 iterator()；数组/String 均非 Iterable 实例
  （`instanceof` 对 final 类 String 是编译错误，须用 `Class.isAssignableFrom` 断言）。
- **默认 spliterator 特征**：未知尺寸 → estimateSize()==Long.MAX_VALUE、getExactSizeIfKnown()==-1。
- **remove() 双面**：不可变实现抛 UnsupportedOperationException 且迭代器仍可用；可变实现
  next() 前调用抛 IllegalStateException，删除"最近返回"的元素（本批自造 MutableList 实证）。
