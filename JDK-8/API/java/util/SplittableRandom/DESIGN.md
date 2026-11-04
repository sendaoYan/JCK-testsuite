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

## 契约深化（第二轮，追加）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | IAE 精确消息："bound must be positive"/"bound must be greater than origin"/"size must be non-negative" | testIaeMessages |
| P0 | 固定 seed=42 的 nextInt() 前 5 值精确锁（与 JDK-27 逐值一致） | testFirstValuesLocked |
| P0 | nextLong/nextDouble/nextBoolean 同 seed 双实例 5 值全等 | testFirstValuesLocked |
| P0 | split() 子实例确定性（同 seed 双亲 split 一次子序列相等）+ 子实例可用 | testSplitContracts |
| P0 | 无 setSeed（种子只经构造）；nextGaussian/nextBytes 在 JDK-8 不存在（反射锁缺席） | testSurfaceContract |

### 易错规范细节（实测锁定）

- SR(42) nextInt 前 5 值 = -491277234, 909395113, -1877322334, -1024560952, -1540993733（**JDK-8 与 JDK-27 相同**）。
- nextDouble(0.0) 消息 JDK-8 = "bound must be positive"，**JDK-27 = "bound must be finite and positive"**（RandomGenerator 默认实现）——两侧各自锁定。
- SplittableRandom 无 setSeed（与 Random 相对）；JDK-8 无 nextGaussian/nextBytes。
