# java.util.concurrent.ThreadLocalRandom 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **实例获取**：`current()`（每线程单例；同线程恒等返回）。
- **有界整数**：`nextInt(bound)`、`nextInt(origin, bound)`；**有界长整**：`nextLong(bound)`、`nextLong(origin, bound)`；**有界浮点**：`nextDouble(bound)`、`nextDouble(origin, bound)`。
- **无界**：`nextInt()`、`nextLong()`、`nextDouble()`、`nextFloat()`、`nextBoolean()`、`nextGaussian()`。
- **流**：`ints/longs/doubles` 的 0/1/2/3 参重载（streamSize 与 origin/bound 组合）。
- **种子**：`setSeed(long)` → **UnsupportedOperationException（核心契约）**。
- **序列化**：Serializable；round-trip 经 readResolve 解析回 current() 实例。

## 2. Java 用户如何使用

- 并行计算/流的水分随机源：各线程独立实例避免 Random 的 CAS 争用；绝不能 setSeed（每线程种子由内部派生）。

## 3. 开源/源码实际用法调研

- JDK 源码 `SplittableRandom`/并发集合随机化用例；OpenJDK 回归 `test/jdk/java/util/concurrent/ThreadLocalRandom/`（含 setSeed UOE 与 IAE 消息）。
- JCK runtime-8b `api/java_util_concurrent/ThreadLocalRandom`。

## 4. 测试用例设计（映射到 ThreadLocalRandomTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | **setSeed → UnsupportedOperationException（JDK-8 实测消息 null）** | testSetSeedUnsupported |
| P0 | nextInt(bound≤0) / nextInt(o≥b) → IAE，消息 "bound must be positive" / "bound must be greater than origin"（实测） | testBoundValidation |
| P0 | nextLong 同上两组消息 | testBoundValidation |
| P0 | nextDouble 单参 IAE 消息 **"bound must be positive"（JDK-8；JDK-17+ 为 "bound must be finite and positive"——版本差异）**；双参同 origin 消息 | testBoundValidation |
| P0 | 合法边界：nextInt(1)==0、nextInt(5,6)==5、ulp 区间 nextDouble | testBoundValidation |
| P0 | 无参方法域断言（int 域/double∈[0,1)/float∈[0,1)/boolean/nextGaussian 有限）×100 聚合 | testUnboundedMethods |
| P0 | 流 0/2/3 参重载：size 精确 + 每值在域内 | testStreams |
| P0 | current() 同线程同实例；**线程隔离：两 daemon 线程各 100 次 nextInt 有界完成** | testThreadIsolation |
| P1 | 序列化 round-trip == current()（readResolve 语义） | testSerialization |
| P1 | **JDK-17+ 的 nextFloat(float)/nextFloat(float,float) 缺席反射断言** | testAbsentNewerMethods |

## 关键规范细节（易错点）

- 不做分布/统计断言（种子不可控），只断域、尺寸与异常契约。
- JDK-8 与 JDK-27 的 nextDouble 单参异常消息不同（见上）；static 结构差异已用 javap 记录（J27 新增 nextFloat 两个带参重载）。
- 测试线程一律 daemon + join/await 有界（≤10s），不留悬挂线程。

## 断言规模

PASS (117 assertions)。
