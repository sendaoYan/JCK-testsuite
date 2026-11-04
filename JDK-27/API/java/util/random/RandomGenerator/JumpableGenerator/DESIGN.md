# java.util.random.RandomGenerator$JumpableGenerator 设计案

## 1. API 功能
可跳跃随机数生成器接口（继承 StreamableGenerator）。`copy()` 复制、`jump()` 前跳、`jumpDistance()` 跳距、`jumps()`/`rngs()` 生成器流、`copyAndJump()` 复制并前跳。

## 2. Java 用户如何使用
`RandomGeneratorFactory.all().filter(isJumpable)` 取实例（如 Xoroshiro128PlusPlus）；`copy()` 保存状态、`jump()` 跳到下一子周期、`copyAndJump()` 返回前跳前的副本并把原生成器前跳。

## 3. 开源代码实际用法调研
JDK 内部：`test/jdk/java/util/random/RandomGenerator` 相关用例与 JEP 356 实现（L64X128MixRandom 等）覆盖 jump/copy 语义。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface + 父接口继承 + 反射核对 copy/jump/leap/splits/rngs 等方法签名。
2. 黑盒：`RandomGeneratorFactory.all().filter(...)` 取真实生成器，断言 copy 共享状态、copyAndJump 返回前跳副本并前跳原生成器、`jumps()`/`jumps(size)`/`rngs()`/`rngs(size)` 流 count。

实现：`JumpableGeneratorTest.java`（`testWhiteBox` + `testBlackBox`）。
