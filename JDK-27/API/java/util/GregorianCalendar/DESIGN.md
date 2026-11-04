# java.util.GregorianCalendar 测试设计

## 1. API 功能
- 构造器：无参、`(TimeZone)`、`(Locale)`、`(TimeZone, Locale)`、`(y,m,d)`、`(y,m,d,h,m,s)`。
- 闰年：`isLeapYear(int)`。
- 类型：`getCalendarType()`（"gregory"）。
- 运算：`add`/`roll`、`getActualMaximum`（继承自 Calendar）。
- java.time 互转：`toZonedDateTime()`、`from(ZonedDateTime)`。

## 2. Java 用户如何使用
- 构造指定日期：`new GregorianCalendar(2024, Calendar.MARCH, 1)`。
- 判断闰年：`gc.isLeapYear(2000)`。
- 月底天数：`gc.getActualMaximum(Calendar.DAY_OF_MONTH)`。
- 与 java.time 互操作：`gc.toZonedDateTime()` / `GregorianCalendar.from(zdt)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/net/HttpCookie.java`：`getExpiryDate()` 内部用 `GregorianCalendar` 计算 cookie 过期时间。
- JDK 源码 `java/text/DateFormat.java`：`Calendar` 实现即 `GregorianCalendar`。
- JDK 源码 `java/text/MessageFormat.java`：日期格式分派到 `GregorianCalendar`。
- 广泛用法：业务代码 `new GregorianCalendar(y, m, d)` 构造特定日期。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言具体类、继承 Calendar、关键方法存在。
- 黑盒（固定 UTC + Locale.US）：
  - `isLeapYear` 闰年/平年/世纪边界。
  - `(y,m,d)` 与 `(y,m,d,h,m,s)` 构造字段。
  - epoch 字段、`getCalendarType`。
  - `add` 跨年 vs `roll` 同年、`getActualMaximum` 闰年/平年。
  - `toZonedDateTime`/`from` 往返（epoch 秒与 UTC zone 不变）。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 契约深化：闰年矩阵/纪元/1582 换历/纯格里历归一化 | testGregorianContracts |
