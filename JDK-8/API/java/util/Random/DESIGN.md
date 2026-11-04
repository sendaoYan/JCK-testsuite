# java.util.Random 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。固定种子保证确定性；不依赖墙钟/默认时区。

## 1. API 功能

- **构造器/种子**：`Random()`、`Random(long)`、`setSeed(long)`。
- **基本随机**：`nextInt()/nextInt(int)/nextLong()/nextBoolean()/nextFloat()/nextDouble()/nextGaussian()`。
- **字节**：`nextBytes(byte[])`。
- **流工厂（JDK 8）**：`ints/longs/doubles` 各 4 重载（无限/带大小/带范围/带大小+范围）。
- **序列化**：`Serializable`（round-trip 保持后续序列一致）。

## 2. 使用方式

`new Random(seed)` → `nextInt(bound)`/`nextLong()`/`nextDouble()`/`nextBytes(bytes)`；
`new Random(seed).ints(streamSize, origin, bound)` 生成流。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Random`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Random.java`：
  `nextInt(bound <= 0)`/`nextBytes(null)`/流参数非法时抛 IAE。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 同种子序列一致 + setSeed 复位 | testSeedReproducibility |
| P0 | 基本 range（nextInt/nextDouble/nextFloat/nextBoolean/nextBytes） | testRanges |
| P0 | nextInt(bound) IAE 矩阵（0/负/MIN_VALUE）+ 2 的幂 + 最大值 | testNextIntBoundContract |
| P0 | nextBytes(null) NPE / 空数组 / 确定性 | testNextBytesContract |
| P1 | nextGaussian 有限 + 确定性 | testNextGaussian |
| P1 | nextDouble/nextFloat 大量采样都在 [0,1) | testNextFloatDoubleContract |
| P1 | ints/longs/doubles 流大小 + 范围 + IAE + 确定性 | testIntStreams / testLongStreams / testDoubleStreams |
| P1 | Serializable round-trip 保持序列 | testSerialization |
| P0 | 契约深化：跨方法全序列确定性闭环 | testNextIntBoundContract、testNextBytesContract、testNextFloatDoubleContract |

## 关键规范细节

- `nextInt(bound <= 0)`、`nextInt(origin >= bound)`、`ints(streamSize < 0)`、`ints(origin >= bound)` 均抛
  `IllegalArgumentException`；`nextBytes(null)` 抛 `NullPointerException`。
- `nextDouble()/nextFloat()` 在 `[0,1)`（含 0 不含 1）；`nextInt(1) == 0`。
- `nextGaussian()` 均值为 0 标准差 1，但断言只做「有限 + 确定性」（不做统计均值，保证确定性）。

## 注意事项

固定种子；不依赖墙钟；流用 `count()`/`allMatch` 断言不依赖迭代顺序；无全局副作用；headless-safe 不加 `@key`。
