# java.util.random.RandomGenerator 测试设计

## 1. API 功能
- 工厂：`of(String)`、`getDefault()`。
- 核心：`nextLong`（抽象）、`nextInt`/`nextLong`/`nextDouble`/`nextFloat`/`nextBoolean`（default）、`nextBytes`、`nextGaussian`、`nextExponential`。
- 流：`ints`/`longs`/`doubles`（多重重载）。
- 元信息：`isDeprecated`。

## 2. Java 用户如何使用
- 默认生成器：`RandomGenerator.getDefault()`。
- 指定算法：`RandomGenerator.of("L32X64MixRandom")`。
- 范围随机：`g.nextInt(origin, bound)`。
- 流：`g.ints(streamSize)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/Random.java`：`Random` 实现 `RandomGenerator`。
- JDK 源码 `java/util/SplittableRandom.java`：实现 `RandomGenerator.SplittableGenerator`。
- JDK 源码 `java/util/random/RandomGenerator.java`：接口默认方法定义。
- 广泛用法：JDK 17+ 统一随机接口。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言接口与关键方法存在。
- 黑盒（固定种子 `factory.create(seed)`）：
  - 工厂 getDefault/of 非空、isDeprecated。
  - 同种子序列一致。
  - nextInt/nextLong/nextDouble/nextFloat 范围、nextBoolean/nextBytes、nextGaussian/nextExponential 有限。
  - ints/longs/doubles 流的数量与范围。
