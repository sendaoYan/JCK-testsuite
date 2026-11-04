# java.util.random.RandomGenerator$SplittableGenerator 设计案

## 1. API 功能
可分裂随机数生成器接口（继承 StreamableGenerator）。`split()`/`split(source)` 分裂、`splits()` 分裂流、`rngs()` 生成器流。

## 2. Java 用户如何使用
`RandomGeneratorFactory.all().filter(isSplittable)` 取实例；`split()` 产生独立子生成器，适合递归并行（如 SplittableRandom）。

## 3. 开源代码实际用法调研
JDK 内部：JEP 356 `RandomGenerator.SplittableGenerator`；`java.util.SplittableRandom` 实现 split；`test/jdk/java/util/random/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface + 父接口继承 + 反射核对 copy/jump/leap/splits/rngs 等方法签名。
2. 黑盒：`RandomGeneratorFactory.all().filter(...)` 取真实生成器，断言 copy 共享状态、copyAndJump/copyAndLeap 与 copy+跳 等价、各类流 count。

实现：`SplittableGeneratorTest.java`（`testWhiteBox` + `testBlackBox`）。
