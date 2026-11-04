# java.util.OptionalInt 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。特化 Optional 无 `ofNullable/map/flatMap/filter`，
> 重点补齐边界值（MIN/MAX/0/-1）、null supplier/consumer 分支、异常对象透传、equals/hashCode/toString。

## 1. API 功能

- **工厂**：`empty()`、`of(int)`。
- **取值/查询**：`getAsInt()`、`isPresent()`。
- **默认/异常**：`orElse(int)`、`orElseGet(IntSupplier)`、`orElseThrow(Supplier)`。
- **副作用**：`ifPresent(IntConsumer)`。
- **相等/输出**：`equals`、`hashCode`、`toString`。

## 2. 使用方式

`OptionalInt.of(x)` / `OptionalInt.empty()`；`opt.orElse(d)`、`opt.orElseGet(() -> d)`、
`opt.orElseThrow(() -> new X(...))`、`opt.ifPresent(c)`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/OptionalInt`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/OptionalInt.java`（与 Optional 相同的短路/null 语义）。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | of/isPresent/getAsInt + empty getAsInt→NSEE | testOfGet |
| P0 | 边界值 MIN/MAX/0/-1 往返 | testBoundaries |
| P0 | orElse/orElseGet/orElseThrow 两态 + supplier 短路 + null 分支 + 异常透传 | testOrElse |
| P0 | ifPresent 两态 + null consumer（present NPE / empty no-op） | testIfPresent |
| P0 | equals/hashCode/toString（含 !equals 两方向） | testEqualsHashCodeToString |

## 关键规范细节

- `orElseGet(null)`/`orElseThrow(null)`：present 短路不抛；empty 抛 NPE。
- `orElseThrow` supplier 返回 null → NPE；否则异常对象透传（`==`）。
- `ifPresent(null)`：present NPE；empty no-op。

## 注意事项

确定性输入；无墙钟/随机/全局副作用；headless-safe 不加 `@key`。

## 5. 契约深化（第二轮追加，映射到新增方法）

| 测试点 | 用例方法 |
|---|---|
| getAsInt NSEE 消息 "No value present" | testExceptionMessages |
| hashCode 公式：empty→0、of(v)→Integer.hashCode(v)（含 MIN/MAX） | testHashCodeFormula |
| equals 跨类型：≠null/Integer/Optional/OptionalLong/OptionalDouble.empty | testEqualsCrossType |
| toString 精确："OptionalInt[-2147483648]"/"[2147483647]"/"[0]"/".empty" | testToStringExact |
| 回调异常同一实例透传（ifPresent/orElseGet）且容器不变 | testCallbackExceptionRelay |
| API 面锁定：无 map/flatMap/ofNullable；JDK-8 无 isEmpty/ifPresentOrElse/stream/orElseThrow() | testSurfaceLock |

### 易错细节

- 特化 Optional 无 `map/flatMap/filter/ofNullable`（与 `Optional<T>` 的面差异，反射锁定）。
- `of(v).hashCode() == v`（Integer.hashCode 即原值）；empty→0。
- equals 只认 OptionalInt：`OptionalInt.of(5).equals(Integer.valueOf(5))==false`。
