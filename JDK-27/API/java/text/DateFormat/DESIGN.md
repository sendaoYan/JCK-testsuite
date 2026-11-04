# java.text.DateFormat 测试方案

## 1. API 功能（按方法分组）

- 常量：字段常量（ERA_FIELD=0 … TIMEZONE_FIELD=17）、样式常量（FULL=0/LONG=1/MEDIUM=2/SHORT=3/DEFAULT=2）。
- 工厂：`getDateInstance()`/`getTimeInstance()`/`getDateTimeInstance()`/`getInstance()` 各自有 0/1/2/3 参重载（样式 + Locale），`getAvailableLocales()`。
- 格式化：`format(Date)`、`format(Date,StringBuffer,FieldPosition)`、继承自 Format 的 `format(Object,...)`。
- 解析：`parse(String)`（抛 ParseException）、`parse(String,ParsePosition)`、`parseObject`。
- 属性：`setCalendar/getCalendar`、`setNumberFormat/getNumberFormat`、`setTimeZone/getTimeZone`、`setLenient/isLenient`。
- 契约：`equals`、`hashCode`、`clone`。

## 2. Java 用户如何使用

- 工厂化实例化：`DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);` 得到适合该 locale 的格式化器。
- 典型调用：`df.format(new Date())` 输出本地化日期/时间；`df.parse("1/1/70")` 解析字符串回 `Date`。
- 组合链路：工厂实例化 → `setTimeZone(UTC)` 固定时区 → `format` 产出确定性字符串 → `parse` 反向还原 → `setLenient(false)` 关宽松解析 → 非法输入抛 `ParseException`；再通过 `setCalendar`/`setNumberFormat` 注入依赖并校验 getter 回读；最后 `clone`/`equals`/`hashCode`。

## 3. 开源代码实际用法调研（来源明确）

- JDK 测试 `test/jdk/java/text/Format/DateFormat/Bug4845901.java`：构造多个 `DateFormat.getDateInstance`/`getTimeInstance` 组合，验证 `format` 结果。
- JDK 源码 `java.util.logging.SimpleFormatter` / 各日志框架普遍使用 `DateFormat.getDateTimeInstance(...)` 生成时间戳。
- Spring `org.springframework.format.datetime` 包装 `DateFormat`，调用 `format`/`parse` 完成类型转换（来源：Spring Framework 公开 API 用法）。
- 通用模式：固定 `Locale` 与 `TimeZone` 后再 `format`/`parse`，避免默认时区/默认 locale 导致的跨机差异（本项目健壮性要求）。

## 4. 测试用例设计 + 自动化实现

实现位置：`DateFormatTest.java`（`testWhiteBox()` 白盒 + `testFactoryFormatStrings()`/`testParseLenientAndProperties()`/`testEqualsCloneHashCode()` 黑盒）。

- 正常链路：四个日期样式 + 三个时间样式 + 两个日期时间组合的确定性输出断言（固定 `Locale.US` + `TimeZone.getTimeZone("UTC")` + `new Date(0L)`；AM/PM 分隔为 U+202F，源码用 ` ` 显式表示）。
- 解析链路：`parse("1/1/70")` 回 `epoch 0`；`setLenient(false)` 后非法输入抛 `ParseException`。
- 属性链路：`setTimeZone/getTimeZone`、`setCalendar/getCalendar`、`setNumberFormat/getNumberFormat` 的注入与回读。
- 契约：`clone` 后 `equals` 为真且 `hashCode` 相等；不同样式/日期与日期时间不相等。
- 健壮性：无墙钟（固定 Date(0)）、固定 locale/timezone、无全局副作用、无文件/网络/端口。headless-safe，不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- 样式常量 FULL=0/LONG=1/MEDIUM=2/SHORT=3/DEFAULT=2 + 全字段常量抽查
- getDate/getTime/getDateTime × 4 样式 × ENGLISH 输出（每侧全等串锁定）
- parse 往返（SHORT 文本→1709769600000L）；calendar/timeZone 接线
- null/非法参数（getDateInstance(-1)→IAE、setCalendar(null)→NPE）
- 版本锁定：JDK-27 时间样式 NNBSP/逗号、ja FULL 日期带星期几（JDK-8 无）——各自断言
