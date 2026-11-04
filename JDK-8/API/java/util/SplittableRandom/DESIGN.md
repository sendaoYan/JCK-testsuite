# java.util.SplittableRandom 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。固定种子保证确定性。

## 1. API 功能

- **构造器**：`SplittableRandom()`、`SplittableRandom(long)`。
- **随机**：`nextInt()/nextInt(int)/nextInt(int,int)`、`nextLong()/nextLong(long)/nextLong(long,long)`、
  `nextDouble()/nextDouble(double)/nextDouble(double,double)`、`nextBoolean()`。
- **拆分**：`split()`。
- **流工厂（JDK 8）**：`ints/longs/doubles` 各 4 重载。

## 2. 使用方式

`new SplittableRandom(seed)` → `nextInt(bound)`/`nextLong(origin,bound)`/`nextDouble(origin,bound)`；
`split()` 派生独立子生成器。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/SplittableRandom`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/SplittableRandom.java`：
  `nextInt(bound <= 0)`、`nextInt(origin >= bound)`、`nextDouble(bound)` 用 `!(bound > 0.0)`（NaN 也抛）、
  `nextDouble(origin,bound)` 用 `!(origin < bound)`（NaN 也抛）。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 同种子序列一致 | testDeterminism |
| P0 | nextInt(bound) + split | testBoundAndSplit |
| P0 | nextInt 全重载 IAE 矩阵 + 2 的幂 + origin/bound 范围 | testNextIntContracts |
| P0 | nextLong 全重载 IAE 矩阵 + 范围 | testNextLongContracts |
| P0 | nextDouble 全重载（NaN/0/负/origin>=bound）+ 范围 | testNextDoubleContracts |
| P1 | nextBoolean 双值 | testNextBoolean |
| P1 | split 独立序列 | testSplitIndependence |
| P1 | ints/longs/doubles 流大小 + 范围 + IAE + 确定性 | testStreams |

## 关键规范细节

- `nextInt(bound <= 0)`/`nextInt(origin >= bound)`/`nextLong`/`nextDouble` 均抛 `IllegalArgumentException`。
- `nextDouble(bound)` 用 `!(bound > 0.0)` 判定，因此 `NaN`、`0.0`、负值都抛 IAE；`nextDouble(origin,bound)`
  用 `!(origin < bound)` 判定，因此任一参数 `NaN` 也抛 IAE。
- `nextDouble()` 在 `[0,1)`；`nextInt(1)==0`、`nextLong(1)==0`。

## 注意事项

固定种子；流用 `count()`/`allMatch` 断言；无全局副作用；headless-safe 不加 `@key`。
