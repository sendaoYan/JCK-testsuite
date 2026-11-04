# java.util.function.DoubleFunction 测试设计案

> 覆盖目标：**完整契约**，覆盖抽象方法、组合/逻辑方法、null 参数、副作用调用证明与短路语义。
> 断言不依赖迭代顺序，headless 安全。

## 1. API 提供了哪些功能

- **抽象方法**：单一声明（apply / test / accept / get 及原始特化）。
- **组合/默认方法**：andThen / compose / and / or / negate / identity / minBy / maxBy（按类型）。
- **静态工厂**：identity / isEqual / not(仅 JDK-11+) / minBy / maxBy。

## 2. Java 用户如何使用

- 用 lambda / 方法引用实例化，作为 map/filter/forEach/collect 的参数。
- 用 `andThen`/`compose` 组合函数链；用 `and`/`or`/`negate` 组合谓词。
- 依赖 null 参数抛 NPE（Objects.requireNonNull），谓词短路求值。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8/27 javadoc**（`java/util/function/*.html`）：各接口异常契约与默认方法语义。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/function` + `~/git/jdk/test/jdk/java/util/function`。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 抽象方法基本映射/判定/消费 | testMain |
| 组合方法顺序（f∘g / g∘f） | testMain |
| 逻辑方法真值表 + 短路 | testMain |
| null 参数 -> NPE | testMain |
| 副作用计数器证明「已存在不调」「absent 不调」 | testMain |
| 异常对象透传 | testMain |

## 摘要

java.util.function.DoubleFunction contract: apply maps double to object.
