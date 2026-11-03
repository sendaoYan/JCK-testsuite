# java.util.SplittableRandom 测试设计

## 1. API 功能
- 构造器：`SplittableRandom()`、`SplittableRandom(long seed)`。
- 核心：`nextInt`/`nextInt(bound)`/`nextInt(origin,bound)`、`nextLong`/`nextLong(bound)`、`nextDouble`、`nextBytes`。
- 分裂：`split()`、`split(source)`、`splits(...)`。
- 流：`ints`/`longs`/`doubles`。

## 2. Java 用户如何使用
- 并行任务每线程一个可复现实例：`new SplittableRandom(seed)`。
- 分裂子流：`SplittableRandom child = parent.split()`。
- 范围随机：`r.nextInt(origin, bound)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/concurrent/ThreadLocalRandom.java`：注释与实现对齐 SplittableRandom 的混合算法。
- JDK 源码 `java/util/random/RandomGeneratorFactory.java`：`SplittableRandom` 是 `SplittableGenerator` 的默认实现之一。
- JDK 自带测试 `test/jdk/java/util/SplittableRandom/SplittableRandomTest.java`：种子复现与 split 一致性。
- 广泛用法（并行蒙特卡洛）：按线程 split 得到独立子序列。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言 final 类、实现 RandomGenerator 与 SplittableGenerator、关键方法存在。
- 黑盒（固定种子）：
  - 同种子序列一致、nextInt/nextLong 确定性。
  - `nextInt(bound)`、`nextInt(origin,bound)`、`nextLong(bound)`、`nextDouble` 范围。
  - split 子序列确定、父序列不受 split 影响、嵌套 split。
