# java.util.OptionalInt 测试方案

白盒结构断言在 `OptionalIntTest.testWhiteBox()`，黑盒行为断言在 `OptionalIntTest.testBlackBox()` 系列方法。

## 1. API 功能（按方法分组）

- **工厂**：`empty()`、`of(int)`。（注意：无 `ofNullable`，与引用型 `Optional` 不同）
- **查询/取值**：`getAsInt()`、`isPresent()`、`isEmpty()`。
- **默认值/异常**：`orElse(int)`、`orElseGet(IntSupplier)`、`orElseThrow()`、`orElseThrow(Supplier)`。
- **副作用/流**：`ifPresent(IntConsumer)`、`ifPresentOrElse(IntConsumer, Runnable)`、`stream()`（返回 `IntStream`）。
- **相等/输出**：`equals`、`hashCode`、`toString`。（无 `filter`/`map`/`flatMap`/`or`）

## 2. Java 用户如何使用

- 构造：`OptionalInt.of(5)`、`OptionalInt.empty()`。
- 典型调用：`opt.getAsInt()`（空抛 `NoSuchElementException`）；`opt.orElse(默认)`；`opt.ifPresent(v -> ...)`；`opt.stream().count()`。
- 真实组合：`IntStream.of(...).min()` 返回 `OptionalInt`，再接 `.orElse(默认)` 兜底。

## 3. 开源代码实际用法调研

> 说明：WebSearch 不可用，调研基于 JDK-27 源码 `~/git/jdk25u-dev` 与 `test/jdk`，逐条标注来源。

- `java/util/stream/IntStream.java:563`（javadoc 示例）：`return foundAny ? OptionalInt.of(result) : OptionalInt.empty();` —— reduce/min/max 的返回值构造。
- `java/util/OptionalInt.java:206`：`return IntStream.of(value);` —— `OptionalInt.stream()` present 分支。
- `test/jdk/java/util/Optional/BasicInt.java`：`checkEmpty`/`checkPresent` 覆盖 `orElse`、`orElseThrow`、`equals`、`toString` 等真实用法。

## 4. 测试用例设计 + 自动化实现

- **正常**：`of/empty/isPresent/isEmpty/getAsInt`；`orElse/orElseGet/orElseThrow(2 形式)`；`ifPresent/ifPresentOrElse/stream`；`equals/hashCode/toString`。
- **组合**：`IntStream.min().orElse(默认)` 兜底链路；present 时 `orElseGet`/`orElseThrow(supplier)` 不调用 supplier。
- **边界/异常**：空 `getAsInt`/`orElseThrow()` 抛 `NoSuchElementException`；`orElseGet` supplier 在 present 时不执行；空 `stream()` 0 元素、present 1 元素。
- **自动化**：`OptionalIntTest.testBlackBox()` 内 `testOfGet/testOrElse/testIfPresentAndStream/testEqualsToString`。
- **健壮性（多运行/并跑/跨平台一致）**：固定 int 输入、无墙钟/无 Random；`toString` 为确定值 `OptionalInt[5]`/`OptionalInt.empty`；无全局副作用/文件/网络/线程；不硬编码平台字符。
