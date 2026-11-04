# java.sql.Timestamp 测试设计案（JDK-8）

> 完整契约：纳秒精度、非法值 IAE、null 语义、toString 尾零截断、equals 不对称、hashCode 不含纳秒、序列化；时区无关。

## 1. API 提供功能
- 构造器（2）：`Timestamp(long)`（毫秒→秒 + 纳秒分解，负数归一化）、`Timestamp(int y,int m,int d,int h,int mi,int s,int nano)`（nano 越界 → IAE）。
- `setTime/getTime/setNanos/getNanos`：纳秒 0..999999999，`setNanos` 越界 → IAE。
- 工厂：`valueOf(String)`（`yyyy-[m]m-[d]d hh:mm:ss[.f...]`，1..9 位小数）、`valueOf(LocalDateTime)`、`from(Instant)`。
- 转换：`toString()`（纳秒尾零截断、最小 `.0`、年份补 4 位）、`toLocalDateTime()`、`toInstant()`。
- 比较：`equals(Timestamp)`、`equals(Object)`（非 Timestamp 恒 false）、`before/after(Timestamp)`、`compareTo(Timestamp/Date/Object)`、`hashCode`（不含纳秒）。

## 2. 使用方式
`valueOf(String/LocalDateTime)` 或 `from(Instant)` 构造 → `toString`/`getNanos`/`getTime` → `equals/compareTo` 比较 → 序列化。

## 3. 参考来源
- javadoc `java/sql/Timestamp.html`；源码 `~/git/jdk8u-dev/.../java/sql/Timestamp.java`；JCK `api/java_sql/Timestamp`；JDK 回归 `test/jdk/java/sql/Timestamp*`。

## 4. 测试用例设计
| 测试点 | 用例方法 |
|---|---|
| 7 参构造器纳秒边界（-1/1e9 → IAE，0/max 合法）+ toString | testIntConstructor |
| `Timestamp(long)` 正/负毫秒纳秒分解 + getTime 往返 | testLongConstructor |
| setTime/getTime/getNanos 状态不变量（含负数归一化） | testSetTimeGetNanos |
| setNanos 边界（-1/1e9 → IAE） | testSetNanos |
| valueOf 合法（无小数/1..9 位/单数字/trim） | testValueOfValid |
| valueOf 非法矩阵（null/无空格/坏日期/10 位小数/裸点/非数字）→ IAE | testValueOfInvalid |
| toString（`.0` 最小、尾零截断、年份补 4 位） | testToString |
| equals/hashCode（纳秒差异、与 java.util.Date 不对称、hashCode 不含纳秒） | testEqualsHashCode |
| compareTo/before/after（纳秒序）+ null → NPE + Object → CCE | testCompareToBeforeAfter |
| valueOf(LocalDateTime)/toLocalDateTime 往返 + null → NPE | testLocalDateTimeConversion |
| from(Instant)/toInstant 往返 + null → NPE | testInstantConversion |
| 序列化 round-trip + serialVersionUID=2745179027874758501L | testSerialization |

## 关键规范细节
- `Timestamp.equals(Object)` 对非 Timestamp 恒 false；`java.util.Date.equals(Timestamp)` 却按毫秒为 true（不对称）。
- `hashCode()` 用底层 util.Date 实现，**不含纳秒**（同秒不同纳秒 hashCode 相等但 equals 不等）。
- `toString()` 纳秒为 0 时仍输出 `.0`；非 0 时截断尾零（`120000000` → `12`）。
- `valueOf(String)` 小数超过 9 位或首字符非数字 → IAE；日期部分月/日边界同 `java.sql.Date`。
- `from(Instant.MAX)` 在 JDK-8 因 long 乘法回绕**不抛** IAE（javadoc 声称会抛，实测不会），故不据此断言。
