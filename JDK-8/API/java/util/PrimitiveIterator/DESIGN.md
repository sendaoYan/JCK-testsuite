# java.util.PrimitiveIterator 测试设计案（JDK-8） 测试设计案

> 覆盖目标：完整契约：原语特化迭代器行为——装箱/特化 next 混用、forEachRemaining 双重载、耗尽 NoSuchElementException、默认方法抛 UOE。

## 1. API 功能

- 接口：泛型骨架：`nextInt()/nextLong()/nextDouble()` 缺省抛 UnsupportedOperationException；`forEachRemaining(Consumer)` 与 `ofInt/ofLong/ofDouble` 默认方法
- `java.util.Iterator` 基础：hasNext/next/remove

## 2. 使用方式

匿名实现 `PrimitiveIterator`（固定元素序列）→ 断言 next 类型/顺序、耗尽 NSEE、forEachRemaining 两个重载都遍历同一序列。

## 3. 参考来源

- Oracle javadoc `java/util/PrimitiveIterator` 及嵌套接口
- JDK 源码 PrimitiveIterator.java 默认实现

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 特化 next 精确值 + hasNext 状态机 | testPrimitiveIterator |
| P0 | 耗尽后 next → NoSuchElementException | testPrimitiveIterator |
| P0 | forEachRemaining(IntConsumer/LongConsumer/DoubleConsumer 与 Consumer 装箱版) | testPrimitiveIteratorContracts |
| P1 | 未实现的原语 next 抛 UnsupportedOperationException（骨架接口） | testPrimitiveIteratorContracts |
| P0 | 契约深化：三种原生迭代器/混合 next()/forEachRemaining 双重载；自定义实现探针/空 NPE/双重载分派/Spliterators 桥接/类型标记 | testPrimitiveIteratorContracts |
