# java.util.Optional 测试设计案（JDK-8）

> 覆盖目标：**完整契约**（Java 8 API 面），不是「方法被调用一次」的冒烟。重点补齐
> null 语义矩阵、callback 是否被调用的副作用证明、单子操作的精确 NPE 契约、equals/hashCode/toString。

## 1. API 提供了哪些功能

- **工厂（3）**：`empty()`、`of(T)`、`ofNullable(T)`。
- **查询/取值**：`get()`、`isPresent()`。
- **默认值/异常**：`orElse(T)`、`orElseGet(Supplier)`、`orElseThrow(Supplier)`。
- **单子操作**：`filter(Predicate)`、`map(Function)`、`flatMap(Function)`。
- **副作用**：`ifPresent(Consumer)`。
- **相等/输出**：`equals`、`hashCode`、`toString`。

（Java 9+ 的 `isEmpty`/`or`/`stream`/`ifPresentOrElse`/`orElseThrow()` 无参不在 JDK-8 面。）

## 2. Java 用户如何使用

- 构造：`Optional.of(v)`（v 不能 null）、`Optional.ofNullable(maybeNull)`、`Optional.empty()`。
- 取默认值：`opt.orElse(d)`、`opt.orElseGet(() -> d)`、`opt.orElseThrow(() -> new X(...))`。
- 单子链：`opt.map(f).filter(p).flatMap(g)`、`opt.ifPresent(consumer)`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/util/Optional.html`）：各方法 null/异常契约。
- **JDK 8 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Optional.java`：
  `ifPresent` 仅在 present 时调 consumer（empty + null consumer 不抛）；`filter/map/flatMap`
  先 `Objects.requireNonNull(function)`（即便 empty 也 NPE）；`orElseGet/orElseThrow` 用
  `value != null ? ... : other.get()` 短路（present 时不调 supplier、null supplier 不抛）。
- **JDK 回归** `~/git/jdk/test/jdk/java/util/Optional`（Basic.java 等）：equals/toString/get 语义。

## 4. 测试用例设计（映射到 OptionalTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 工厂：of(null)→NPE、ofNullable(null)→empty、ofNullable(v)==of(v) | testCreation |
| P0 | get 两态 + empty get→NSEE；isPresent 两态 | testGetIsPresent |
| P0 | orElse/orElseGet/orElseThrow 两态 + supplier 短路证明 + null supplier 分支 + 异常对象透传 | testOrElse |
| P0 | ifPresent 两态 + null consumer 分支（present NPE / empty no-op） | testIfPresent |
| P0 | filter 保留/丢弃/empty 不调 predicate + filter(null) 两态 NPE | testFilter |
| P0 | map 变换/null 结果→empty + map(null) 两态 NPE | testMap |
| P0 | flatMap 结果空/null 结果→NPE + flatMap(null) 两态 NPE | testFlatMap |
| P0 | equals/hashCode/toString（含 !equals 非 Optional） | testEqualsHashCodeToString |

## 关键规范细节（易错，已锁入断言）

- `ifPresent(null)`：present → NPE；empty → no-op（javadoc 明确「仅 present 且 consumer 为 null 时抛」）。
- `filter(null)`/`map(null)`/`flatMap(null)`：**无论 present/empty 都抛 NPE**（先 requireNonNull）。
- `map` 函数返回 null → 结果为 empty（内部 `ofNullable`）；`flatMap` 函数返回 null → NPE。
- `orElseGet(null)`/`orElseThrow(null)`：present 时短路**不抛**；empty 时抛 NPE。
- `orElseThrow(supplier)` 空时 supplier 返回 null → `throw null` 抛 NPE；否则原始异常对象透传（`==`）。
- `orElse(null)` 合法：empty 时返回 null，present 时返回 value。

## 注意事项

- 确定性：固定字符串输入；equals/hashCode/toString 为确定值；无墙钟/随机/文件/网络/线程/全局副作用。
- headless-safe：不加 `@key`。
