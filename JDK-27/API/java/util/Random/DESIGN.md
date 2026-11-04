# java.util.Random 测试设计

## 1. API 功能
- 构造器：`Random()`、`Random(long seed)`、`from(RandomGenerator)`。
- 核心：`setSeed`、`nextInt`/`nextInt(bound)`、`nextLong`、`nextBoolean`、`nextFloat`、`nextDouble`、`nextGaussian`、`nextBytes`。
- 流：`ints`/`longs`/`doubles`（多重重载）。

## 2. Java 用户如何使用
- 可复现随机：`new Random(seed)` 生成固定序列。
- 边界随机：`r.nextInt(n)` 生成 `[0, n)`。
- 重置种子：`r.setSeed(seed)`。
- 批量：`r.ints(count).toArray()`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/lang/Math.java`：`StrictMath`/`Math` 的 `random()` 委托 `java.util.Random`。
- JDK 源码 `java/util/Random.java`：自身实现与 `RandomGenerator` 桥接。
- JDK 自带测试 `test/jdk/java/util/Random/RandomTest.java`：种子复现、分布范围。
- 广泛用法（游戏/模拟）：`new Random(seed)` 保证测试可复现。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言具体类、实现 Serializable 与 RandomGenerator、关键方法存在。
- 黑盒（固定种子）：
  - 同种子序列一致、不同种子不同。
  - `nextInt(bound)` 范围、`nextInt(0)` 抛 IAE。
  - `nextDouble`/`nextFloat` 在 [0,1)、`nextBytes` 填充。
  - `setSeed` 重置序列、`ints`/`longs`/`doubles` 流确定性、`nextGaussian` 确定性。
