# java.sql.Date 测试设计案（JDK-8）

> 覆盖目标：完整契约（构造器/工厂边界 + 精确异常 + null 语义 + 日期语义 + 跨实现 equals + 序列化），
> 不是「方法调用一次」的冒烟。所有断言**与时区无关**（不依赖默认 TimeZone）。

## 1. API 提供了哪些功能

- 构造器（2）：`Date(long)`（保留毫秒原值，不真正抹零时间分量）、`Date(int year,int month,int day)`（deprecated，委托 `java.util.Date`，越界结果未定义）。
- 工厂：`valueOf(String)`（JDBC 转义格式 `yyyy-[m]m-[d]d`，月/日可省前导零）、`valueOf(LocalDate)`。
- 转换：`toString()`（恒 `yyyy-mm-dd`）、`toLocalDate()`。
- 时间访问/修改器全部抛 `IllegalArgumentException`：`getHours/getMinutes/getSeconds/setHours/setMinutes/setSeconds`。
- `toInstant()` 抛 `UnsupportedOperationException`（SQL DATE 无时间分量）。
- 继承 `java.util.Date`：`equals/hashCode/compareTo/before/after/getTime/setTime`、`Serializable`。

## 2. Java 用户如何使用

`Date.valueOf("yyyy-mm-dd")` 解析；`toString()` 输出；`valueOf(LocalDate)/toLocalDate()` 与 java.time 互转；
`getTime()/equals/compareTo` 与 `java.util.Date` 互通；`Serializable` 序列化。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/sql/Date.html`。
- OpenJDK 8u 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/sql/Date.java`（valueOf 边界、时间方法抛 IAE、toInstant 抛 UOE）。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_sql/Date`（等价类 + 序列化）。

## 4. 测试用例设计（映射到 DateTest.java 方法）

| 测试点 | 用例方法 |
|---|---|
| 构造器矩阵：`Date(long)` 0/±1/1000/Long.MIN/MAX getTime 往返；`Date(int,int,int)` 闰日/toString | testConstructors |
| valueOf 合法格式 + 单数字月/日规范化 | testValueOfValid |
| valueOf 非法矩阵（null/空/年长度/月 0/13/日 0/32/多分隔/非数字）→ IAE | testValueOfInvalid |
| valueOf 日溢出规范化（2019-02-29→03-01） | testValueOfNormalization |
| deprecated 时间访问/修改器全部 → IAE | testDeprecatedTimeAccessorsThrow |
| toInstant → UOE | testToInstantThrows |
| valueOf(LocalDate)/toLocalDate 往返 + null → NPE | testLocalDateConversion |
| equals/hashCode 跨实现（java.util.Date、Timestamp 不对称） | testEqualsHashCode |
| compareTo/before/after + compareTo(null)→NPE | testCompareToBeforeAfter |
| 序列化 round-trip + serialVersionUID=1511598038487230103L | testSerialization |

## 关键规范细节（易错，已锁入断言）

- `Date.valueOf(String)` 只做 1..12 月、1..31 日边界校验；**不做「月-日」合法性校验**，
  溢出由 deprecated `java.util.Date` 构造器规范化（`2019-02-29` → `2019-03-01`）。
- `Date(long)` 与 `setTime(long)` 在 JDK-8 并不真正抹掉时分秒（源码注释与实现不一致），
  只断言 getTime 往返，不断言 toString。
- 时间分量方法抛 `IllegalArgumentException`，但 `toInstant()` 抛 `UnsupportedOperationException`（两者不同）。
- `Date.equals(Timestamp)` 按毫秒比较为 true（继承 util.Date.equals），而 `Timestamp.equals(Date)` 恒 false（不对称）。
