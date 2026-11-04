# java.util.Date 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。**固定 Instant、不依赖墙钟**：用 `Date(long)`、
> `Date.UTC(...)`、`Instant.ofEpochMilli(...)` 构造固定时间点；弃用字段方法只做「set 后 get」的
> 本地时区无关往返断言，不断言绝对字段值。

## 1. API 功能

- **构造器**：`Date()`、`Date(long)`、弃用 `Date(int,int,int)`、`Date(int,int,int,int,int)`、
  `Date(int,int,int,int,int,int)`、`Date(String)`。
- **时间值**：`getTime()`、`setTime(long)`。
- **顺序**：`before(Date)`、`after(Date)`、`compareTo(Date)`。
- **相等/哈希**：`equals`、`hashCode`、`clone()`。
- **Instant 桥接**：`from(Instant)`、`toInstant()`。
- **静态**：`UTC(int,...)`、`parse(String)`。
- **弃用字段方法**：`getYear/setYear`、`getMonth/setMonth`、`getDate/setDate`、`getDay`、
  `getHours/setHours`、`getMinutes/setMinutes`、`getSeconds/setSeconds`、`toGMTString`、
  `toLocaleString`、`getTimezoneOffset`。

## 2. 使用方式

`new Date(millis)`；`d.getTime()/setTime()`；`d.before/after/compareTo`；`Date.from(instant)`；
`instant = d.toInstant()`；`Date.UTC(...)` 计算 UTC 毫秒。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Date`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Date.java`：
  弃用字段方法通过 `cdate`（默认时区）读写，`setX` 后 `getX` 往返与默认时区无关；
  `clone` 深拷贝 `cdate`；`toInstant` 等价 `Instant.ofEpochMilli(fastTime)`。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器/时间值：long 构造器 MIN/MAX/0/-1、setTime 往返、弃用字段构造器往返 | testConstructorsAndTime |
| P0 | 顺序：compareTo 符号/自反、before/after、null NPE、equals/hashCode、传递性 | testOrdering |
| P0 | clone 独立（改 clone 不影响原对象） | testClone |
| P0 | from/toInstant 往返 + MIN/MAX + from(null) NPE | testInstant |
| P0 | UTC 固定 epoch 值 | testUtc |
| P1 | 弃用字段 set/get 往返 + getDay 范围 | testDeprecatedFieldRoundtrip |
| P1 | Serializable 往返（含 MIN_VALUE）+ 复活后可用 | testSerialization |
| P0 | 契约深化：hashCode 公式/排序矩阵/parse/UTC 静态 | testDateContracts |

## 关键规范细节

- `compareTo(null)`/`before(null)`/`after(null)`/`from(null)` → NPE。
- `clone()` 返回独立对象（`cdate` 深拷贝）；改 clone 不影响原对象。
- `toInstant()` 对任意 `long`（含 MIN/MAX）等于 `Instant.ofEpochMilli(fastTime)`。
- `Date.UTC(70,0,1,0,0,0) == 0`；弃用字段方法不做绝对 TZ 断言。

## 注意事项

固定 Instant；无墙钟依赖（仅 `new Date().getTime() >= 0` 弱断言）；无随机/全局副作用；
headless-safe 不加 `@key`。
