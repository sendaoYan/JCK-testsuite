# java.util.OptionalDouble 测试方案

白盒结构断言在 `OptionalDoubleTest.testWhiteBox()`，黑盒行为断言在 `OptionalDoubleTest.testBlackBox()` 系列方法。

## 1. API 功能（按方法分组）

- **工厂**：`empty()`、`of(double)`。（无 `ofNullable`）
- **查询/取值**：`getAsDouble()`、`isPresent()`、`isEmpty()`。
- **默认值/异常**：`orElse(double)`、`orElseGet(DoubleSupplier)`、`orElseThrow()`、`orElseThrow(Supplier)`。
- **副作用/流**：`ifPresent(DoubleConsumer)`、`ifPresentOrElse(DoubleConsumer, Runnable)`、`stream()`（返回 `DoubleStream`）。
- **相等/输出**：`equals`、`hashCode`、`toString`。（无 `filter`/`map`/`flatMap`/`or`）

## 2. Java 用户如何使用

- 构造：`OptionalDouble.of(2.5)`、`OptionalDouble.empty()`。
- 典型调用：`opt.getAsDouble()`（空抛 `NoSuchElementException`）；`opt.orElse(默认)`；`opt.ifPresent(v -> ...)`。
- 真实组合：`DoubleStream.of(...).min()` 返回 `OptionalDouble`，再接 `.orElse(默认)`。

## 3. 开源代码实际用法调研

> 说明：WebSearch 不可用，调研基于 JDK-27 源码 `~/git/jdk25u-dev` 与 `test/jdk`，逐条标注来源。

- `java/util/stream/DoubleStream.java`（javadoc，如 `reduce`/`min`/`max`/`average` 段落）：返回 `OptionalDouble`，空流返回空 `OptionalDouble`。
- `java/util/OptionalDouble.java`：`stream()` present 分支返回 `DoubleStream.of(value)`。
- `test/jdk/java/util/Optional/BasicDouble.java`：`checkEmpty`/`checkPresent` 覆盖 `orElse`、`orElseThrow`、`equals`、`toString`。

## 4. 测试用例设计 + 自动化实现

- **正常**：`of/empty/isPresent/isEmpty/getAsDouble`；`orElse/orElseGet/orElseThrow(2 形式)`；`ifPresent/ifPresentOrElse/stream`；`equals/hashCode/toString`。
- **组合**：`DoubleStream.min().orElse(默认)`；present 时 `orElseGet`/`orElseThrow(supplier)` 不调用 supplier。
- **边界/异常**：空 `getAsDouble`/`orElseThrow()` 抛 `NoSuchElementException`；空 `stream()` 0 元素、present 1 元素。
- **自动化**：`OptionalDoubleTest.testBlackBox()` 内 `testOfGet/testOrElse/testIfPresentAndStream/testEqualsToString`。
- **健壮性（多运行/并跑/跨平台一致）**：固定 double 输入（2.5 可精确表示）、无墙钟/无 Random；`toString` 为确定值 `OptionalDouble[2.5]`/`OptionalDouble.empty`；无全局副作用/文件/网络/线程；不测超越函数。
