# java.util.OptionalDouble 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。特化 Optional 无 `ofNullable/map/flatMap/filter`，
> 重点补齐浮点边界（NaN/±Inf/±0.0/MIN_VALUE）、Double.compare 的 equals 语义、null 分支、toString。

## 1. API 功能

- **工厂**：`empty()`、`of(double)`。
- **取值/查询**：`getAsDouble()`、`isPresent()`。
- **默认/异常**：`orElse(double)`、`orElseGet(DoubleSupplier)`、`orElseThrow(Supplier)`。
- **副作用**：`ifPresent(DoubleConsumer)`。
- **相等/输出**：`equals`、`hashCode`、`toString`。

## 2. 使用方式

`OptionalDouble.of(x)` / `OptionalDouble.empty()`；`opt.orElse(d)`、`opt.orElseGet(() -> d)`、
`opt.orElseThrow(() -> new X(...))`、`opt.ifPresent(c)`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/OptionalDouble`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/OptionalDouble.java`。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | of/isPresent/getAsDouble + empty getAsDouble→NSEE | testOfGet |
| P0 | 边界值 0.0/-0.0/NaN/±Inf/MIN_VALUE | testBoundaries |
| P0 | orElse/orElseGet/orElseThrow 两态 + supplier 短路 + null 分支 + 异常透传 | testOrElse |
| P0 | ifPresent 两态 + null consumer（present NPE / empty no-op） | testIfPresent |
| P0 | equals/hashCode/toString：Double.compare 语义（NaN==NaN、+0.0!=-0.0） | testEqualsHashCodeToString |

## 关键规范细节

- `equals` 用 `Double.compare`：`NaN` 等于 `NaN`，`+0.0` 不等于 `-0.0`。
- `getAsDouble` 用 `==`：`NaN != NaN`。
- `orElseGet(null)`/`orElseThrow(null)`：present 短路不抛；empty 抛 NPE。
- `orElseThrow` supplier 返回 null → NPE；否则异常对象透传（`==`）。

## 注意事项

确定性输入；无墙钟/随机/全局副作用；headless-safe 不加 `@key`。

## 5. 契约深化（第二轮追加，映射到新增方法）

| 测试点 | 用例方法 |
|---|---|
| getAsDouble NSEE 消息 "No value present" | testExceptionMessages |
| hashCode 公式：empty→0、of(0.0)→0、of(-0.0)→Integer.MIN_VALUE、of(1.0)→1072693248、NaN→Double.hashCode(NaN) | testHashCodeFormula |
| equals 跨类型 + Double.compare 语义（NaN 自反、±0 互斥、对称） | testEqualsCrossType |
| toString 精确："OptionalDouble[-0.0]"/"[NaN]"/"[Infinity]"/"[-Infinity]"/"[1.5E300]" | testToStringExact |
| orElse 特殊值按位透传（NaN、-0.0 判 1/x） | testOrElseSpecialValues |
| 回调异常同一实例透传且容器不变 | testCallbackExceptionRelay |
| API 面锁定：无 map/flatMap/ofNullable/isNaN/isInfinite/getAsInt（JDK-8 无 isEmpty/stream/ifPresentOrElse/orElseThrow()） | testSurfaceLock |

### 易错细节

- equals 用 `Double.compare`：NaN equals NaN、+0.0 ≠ -0.0；而 hashCode 用 Double.hashCode（位模式），
  与 equals 语义一致（不同 hash ⇒ 必不等）。
- `of(-0.0).hashCode() == Integer.MIN_VALUE`（高位字符号位）；`of(1.0).hashCode() == 0x3FF00000`。
