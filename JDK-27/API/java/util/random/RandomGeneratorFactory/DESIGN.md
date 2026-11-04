# java.util.random.RandomGeneratorFactory 测试设计

## 1. API 功能
- 发现：`all()`、`getDefault()`、`of(String)`。
- 元信息：`name`、`group`、`stateBits`、`equidistribution`、`period`、`isStatistical`、`isStochastic`、`isHardware`、`isSplittable`、`isStreamable`、`isDeprecated` 等。
- 创建：`create()`、`create(long seed)`、`create(byte[] seed)`。

## 2. Java 用户如何使用
- 枚举算法：`RandomGeneratorFactory.all()`。
- 按名创建：`RandomGeneratorFactory.of("L32X64MixRandom").create(seed)`。
- 查询特性：`factory.isSplittable()`、`factory.period()`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/Random.java`：`from(RandomGenerator)` 桥接工厂创建。
- JDK 源码 `java/util/SplittableRandom.java`：由 `RandomGeneratorFactory` 通过 SPI 暴露。
- JDK 自带测试 `test/jdk/java/util/Random/RandomGeneratorFactoryTest.java`：发现与元数据。
- 广泛用法：选择特定随机算法的库。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言 final 类、关键方法存在。
- 黑盒（固定算法名 L32X64MixRandom 与固定种子）：
  - all() 非空、getDefault()、of(name) 与 name() 往返。
  - group/stateBits/equidistribution/period 元数据、isStatistical/isStochastic/isHardware/isStreamable/isDeprecated。
  - create()/create(long)/create(byte[]) 非空、不同种子不同、of(name()) 往返。
  - 同种子序列一致。
