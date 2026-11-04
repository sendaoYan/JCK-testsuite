# java.util.Optional 测试方案

白盒结构断言在 `OptionalTest.testWhiteBox()`，黑盒行为断言在 `OptionalTest.testBlackBox()` 系列方法。

## 1. API 功能（按方法分组）

- **工厂**：`empty()`、`of(T)`、`ofNullable(T)`。
- **查询/取值**：`get()`、`isPresent()`、`isEmpty()`。
- **默认值/异常**：`orElse(T)`、`orElseGet(Supplier)`、`orElseThrow()`、`orElseThrow(Supplier)`。
- **单子操作**：`filter(Predicate)`、`map(Function)`、`flatMap(Function)`、`or(Supplier)`、`stream()`。
- **副作用**：`ifPresent(Consumer)`、`ifPresentOrElse(Consumer, Runnable)`。
- **相等/输出**：`equals`、`hashCode`、`toString`。

## 2. Java 用户如何使用

- 构造：`Optional.of(value)`（value 不能 null）、`Optional.ofNullable(maybeNull)`、`Optional.empty()`。
- 典型调用：`opt.isPresent() && opt.get()`；`opt.orElse(default)`；`opt.orElseThrow(() -> new X(...))`；`opt.map(f).orElse(d)`；`opt.ifPresent(consumer)`。
- 真实组合：`Optional.ofNullable(name).map(String::trim).filter(s -> !s.isEmpty()).orElse("default")`；`opt.flatMap(Optional::stream)`。

## 3. 开源代码实际用法调研

> 说明：WebSearch 不可用，调研基于 JDK-27 源码 `~/git/jdk25u-dev` 与 `test/jdk`，逐条标注来源。

- `java/util/Calendar.java:1680-1685`：`TimeZoneNameUtility.convertLDMLShortID(shortTZID).map(TimeZone::getTimeZone).orElse(defaultTZ)` —— map + orElse。
- `java/util/ServiceLoader.java:1656-1663`：`if (iterator.hasNext()) { return Optional.of(iterator.next()); } else { return Optional.empty(); }` —— Optional.of/empty。
- `java/util/Locale.java:1186`：`return Optional.ofNullable(exts);` —— ofNullable。
- `java/util/Optional.java:327`（javadoc）：`Stream<T> s = os.flatMap(Optional::stream)` —— flatMap + stream。
- `test/jdk/java/util/Optional/Basic.java` 等覆盖 Optional 的 get/orElse/equals/toString 语义。

## 4. 测试用例设计 + 自动化实现

- **正常**：`empty/of/ofNullable`；`get/isPresent/isEmpty`；`orElse/orElseGet/orElseThrow(2 形式)`；`filter/map/flatMap/or/stream/ifPresent/ifPresentOrElse`；`equals/hashCode/toString`。
- **组合**：`ofNullable(name).map(trim).filter(非空).orElse(default)`；`flatMap(Optional::stream)`；`or` 在 present 时不调用 supplier。
- **边界/异常**：`of(null)` 抛 NPE；`or(null)` 抛 NPE；空 `get`/`orElseThrow()` 抛 `NoSuchElementException`；`orElseGet`/`or` 的 supplier 在 present 时不执行；空 `stream()` 0 元素、present 1 元素。
- **自动化**：`OptionalTest.testBlackBox()` 内 `testCreation/testGetOrElse/testMonadic/testOrStreamIfPresentOrElse/testIfPresentEqualsToString`。
- **健壮性（多运行/并跑/跨平台一致）**：固定字符串输入、无墙钟/无 Random；equals/hashCode/toString 均为确定值（`Optional[x]`/`Optional.empty`）；无全局副作用/文件/网络/线程；不硬编码平台字符。
