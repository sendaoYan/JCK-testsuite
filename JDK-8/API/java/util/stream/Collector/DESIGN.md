# java.util.stream.Collector 测试设计案

> 覆盖目标：**完整契约**。断言不依赖迭代顺序，headless 安全，固定输入确定性。

## 1. API 提供了哪些功能

Collector 接口与 of() 工厂、Characteristics 枚举、supplier/accumulator/combiner/finisher。

## 2. Java 用户如何使用

- 用工厂方法或 StreamSupport 创建流，链式中间操作后接一个终止操作。
- 依赖惰性求值（中间操作在终止操作前不执行）、单次消费（重复消费抛 IllegalStateException）。
- 依赖空流语义（sum=0、average/min/max 为空 Optional、reduce 返回空）。
- 依赖 null 参数抛 NPE、limit/skip 负数抛 IllegalArgumentException。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8/27 javadoc**（`java/util/stream/*.html`）：各方法异常契约、惰性/单次消费语义。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/stream` + `~/git/jdk/test/jdk/java/util/stream`。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 工厂方法边界 | testFactories |
| 惰性求值 / 单次消费 | testLaziness / testSingleUse |
| 中间操作链 | testIntermediate |
| 终止操作 + 空流语义 | testTerminal |
| 并行/串行一致 | testParallelConsistency |
| null/坏参数 -> NPE/IAE | testNullAndBadArgs |
| JDK-27 专属方法 | testJava9Plus |
