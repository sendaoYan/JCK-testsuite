# java.util.random.RandomGenerator$LeapableGenerator 设计案

## 1. API 功能
可大跳随机数生成器接口（继承 JumpableGenerator）。`leap()` 大跳、`leapDistance()` 大跳距、`leaps()` 生成器流、`copyAndLeap()` 复制并大跳。

## 2. Java 用户如何使用
`RandomGeneratorFactory.all().filter(isLeapable)` 取实例（如 Xoroshiro128PlusPlus）；`leap()` 跳到极远子周期，适合大规模并行分区。

## 3. 开源代码实际用法调研
JDK 内部：JEP 356 `RandomGenerator.LeapableGenerator`；`test/jdk/java/util/random/` 覆盖 leap 与 copyAndLeap。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface + 父接口继承 + 反射核对 copy/jump/leap/splits/rngs 等方法签名。
2. 黑盒：`RandomGeneratorFactory.all().filter(...)` 取真实生成器，断言 copy 共享状态、copyAndLeap 返回大跳前副本并大跳原生成器、`leaps()`/`leaps(size)` 流 count。

实现：`LeapableGeneratorTest.java`（`testWhiteBox` + `testBlackBox`）。
