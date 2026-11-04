# java.util.PrimitiveIterator.OfInt 测试设计案（JDK-27）

> 覆盖目标：完整契约：原语特化迭代器行为——特化/装箱 next 混用、forEachRemaining 双重载、耗尽 NoSuchElementException、骨架默认方法 UOE。

## 1. API 功能

- 接口：`nextInt()` 特化与 `next()` 装箱、`forEachRemaining(IntConsumer)` 与 `forEachRemaining(Consumer)` 互调
- `java.util.Iterator` 基础：hasNext/next/remove

## 2. 使用方式

匿名实现固定元素序列 → 断言 next 顺序与类型、耗尽 NSEE、forEachRemaining 两个重载遍历同一序列。

## 3. 参考来源

- Oracle javadoc `java/util/PrimitiveIterator` 及嵌套接口
- JDK 源码 PrimitiveIterator.java 默认实现

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 特化 next 精确值 + hasNext 状态机 | testOfInt |
| P0 | 耗尽后 next → NoSuchElementException | testOfInt |
| P0 | forEachRemaining 原语 Consumer 与装箱 Consumer 重载 | testOfIntContracts |
| P1 | 未实现的原语 next 抛 UnsupportedOperationException（骨架接口） | testOfIntContracts |
| P0 | 契约深化：next 混用/装箱 Consumer 重载/耗尽异常 | testOfIntContracts |
