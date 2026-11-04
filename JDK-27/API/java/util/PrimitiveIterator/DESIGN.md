# java.util.PrimitiveIterator — 测试设计案

## 1. API 功能
原始类型迭代器的基接口，避免装箱。继承 Iterator。

- abstract：forEachRemaining(T_CONS)。
- 子接口：OfInt/OfLong/OfDouble，nextInt/nextLong/nextDouble + default next/forEachRemaining。

## 2. Java 用户如何使用
`PrimitiveIterator.OfInt it = IntStream.range(0, 3).iterator();` nextInt 取原始值、
forEachRemaining(IntConsumer) 消费。
链路：IntStream.iterator -> nextInt -> forEachRemaining。

## 3. 开源代码实际用法调研
- OpenJDK `PrimitiveIterator.OfInt`（本地 `~/git/jdk25u-dev`）：nextInt 抽象、next 默认装箱、
  forEachRemaining 默认循环 nextInt——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/stream/`.

## 4. 测试用例设计 + 自动化实现（PrimitiveIteratorTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 接口性 | OfInt 继承 PrimitiveIterator/Iterator |
| nextInt | 无装箱返回 int |
| forEachRemaining | IntConsumer 消费剩余 |
| 耗尽 | hasNext false |
| P0 | 契约深化：三种原生迭代器/混合 next()/forEachRemaining 双重载；自定义实现探针/空 NPE/双重载分派/Spliterators 桥接/类型标记 | testPrimitiveIteratorContracts |

共 4 组场景、约 7 个 check（`testBlackBox()`）。
