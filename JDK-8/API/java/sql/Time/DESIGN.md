# java.sql.Time 测试设计案（JDK-8）

> 完整契约：构造器/工厂边界 + 精确异常 + null 语义 + 时间语义 + 跨实现 equals + 序列化；时区无关。

## 1. API 提供功能
- 构造器（2）：`Time(long)`（毫秒原值）、`Time(int h,int m,int s)`（deprecated，委托 util.Date，越界规范化）。
- 工厂：`valueOf(String)`（`hh:mm:ss`）、`valueOf(LocalTime)`。
- 转换：`toString()`（恒 `hh:mm:ss`）、`toLocalTime()`。
- 日期访问/修改器全部抛 IAE：`getYear/getMonth/getDay/getDate/setYear/setMonth/setDate`。
- `toInstant()` 抛 UOE。继承 `java.util.Date`。

## 2. 使用方式
`valueOf("hh:mm:ss")` 解析 → `toString()` 输出 → `toLocalTime()` 与 java.time 互转；`getTime/equals/compareTo` 通用。

## 3. 参考来源
- javadoc `java/sql/Time.html`；源码 `~/git/jdk8u-dev/.../java/sql/Time.java`；JCK `api/java_sql/Time`。

## 4. 测试用例设计
| 测试点 | 用例方法 |
|---|---|
| 构造器矩阵：`Time(long)` 往返 + `Time(int,int,int)` 0/23 边界 | testConstructors |
| valueOf 合法 + 单数字规范化 | testValueOfValid |
| valueOf 非法矩阵（null/空/缺冒号/多冒号/非数字）→ IAE | testValueOfInvalid |
| valueOf 越界规范化（25→01、24→00、61 分→01:01、61 秒→00:01:01） | testValueOfNormalization |
| deprecated 日期访问/修改器 → IAE | testDeprecatedDateAccessorsThrow |
| toInstant → UOE | testToInstantThrows |
| valueOf(LocalTime)/toLocalTime 往返 + null → NPE | testLocalTimeConversion |
| equals/hashCode 跨实现（java.util.Date） | testEqualsHashCode |
| compareTo/before/after + null → NPE | testCompareToBeforeAfter |
| 序列化 round-trip + serialVersionUID=8397324403548013681L | testSerialization |

## 关键规范细节
- `Time.valueOf(String)` 不校验 h/m/s 范围（只校验「两个冒号」结构），越界由 util.Date 规范化。
- 日期分量方法抛 IAE；`toInstant()` 抛 UOE。
