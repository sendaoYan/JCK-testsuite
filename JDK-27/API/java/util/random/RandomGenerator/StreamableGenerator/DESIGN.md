# java.util.random.RandomGenerator$StreamableGenerator 设计案

## 1. API 功能
可流式随机数生成器接口（继承 RandomGenerator）。`rngs()`/`rngs(long)` 产生 RandomGenerator 流；静态 `of(String)` 按算法名创建。

## 2. Java 用户如何使用
`RandomGeneratorFactory.all().filter(isStreamable)` 取实例；`rngs()` 生成一列独立生成器；`StreamableGenerator.of(name)` 按名创建。

## 3. 开源代码实际用法调研
JDK 内部：JEP 356 `RandomGenerator.StreamableGenerator`；`test/jdk/java/util/random/` 覆盖 rngs 流。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface + 父接口继承 + 反射核对 copy/jump/leap/splits/rngs 等方法签名。
2. 黑盒：`RandomGeneratorFactory.all().filter(...)` 取真实生成器，断言 copy 共享状态、copyAndJump/copyAndLeap 与 copy+跳 等价、各类流 count。

实现：`StreamableGeneratorTest.java`（`testWhiteBox` + `testBlackBox`）。
