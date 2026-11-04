# java.util.random.RandomGenerator$ArbitrarilyJumpableGenerator 设计案

## 1. API 功能
可任意距离跳跃随机数生成器接口（继承 LeapableGenerator）。`jump(double)`/`jumpPowerOfTwo(int)` 任意前跳、`jumps(double)` 流、`copyAndJump(double)`。

## 2. Java 用户如何使用
`RandomGeneratorFactory.all().filter(isArbitrarilyJumpable)` 取实例；`jump(distance)` 按距离跳、`jumpPowerOfTwo(p)` 跳 2^p。注意本 JDK-27 构建中没有任何算法实现该接口（`isArbitrarilyJumpable()` 全部为 false），因此测试用共享可变状态的最小匿名实现验证契约。

## 3. 开源代码实际用法调研
JDK 内部：JEP 356 `RandomGenerator.ArbitrarilyJumpableGenerator`；`test/jdk/java/util/random/` 覆盖 jump 距离与复制。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface + 父接口继承 + 反射核对 copy/jump/leap/splits/rngs 等方法签名。
2. 黑盒：以共享可变状态的最小匿名实现验证 `copy()` 共享状态、`copyAndJump(d)` 返回前跳副本并前跳原生成器、`jump(double)`/`jumpPowerOfTwo(int)`/`jump()`/`leap()` 可用、`jumps(d)`/`jumps(size,d)` 流 count。

实现：`ArbitrarilyJumpableGeneratorTest.java`（`testWhiteBox` + `testBlackBox`）。
