# java.util.Calendar 测试设计

## 1. API 功能
- 工厂：`getInstance()`、`getInstance(TimeZone, Locale)`。
- 字段读写：`get(int)`、`set(int,int)`、`set(y,m,d,...)`、`clear()`。
- 时间：`getTimeInMillis()`、`setTimeInMillis(long)`、`getTime()`、`setTime(Date)`。
- 运算：`add(field, amount)`（进位）、`roll(field, amount)`（不进位）、`getActualMaximum`。
- 比较/复制：`before`、`after`、`compareTo`、`equals`、`clone`。

## 2. Java 用户如何使用
- 取字段：`Calendar c = Calendar.getInstance(); c.get(Calendar.YEAR)`。
- 计算：`c.add(Calendar.MONTH, 1)`（跨年）vs `c.roll(Calendar.MONTH, true)`（同年）。
- 月底天数：`c.getActualMaximum(Calendar.DAY_OF_MONTH)`。
- 毫秒互转：`c.setTimeInMillis(ms)` / `c.getTimeInMillis()`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/text/SimpleDateFormat.java`：内部用 `Calendar` 做日期字段解析与格式化。
- JDK 源码 `java/text/DateFormat.java`：`getCalendar()`/`setCalendar` 组合。
- JDK 源码 `java/time/chrono/JapaneseDate.java`、`Chronology.java`：桥接 `Calendar` 与 java.time。
- 广泛用法（业务代码）：`Calendar.getInstance(); cal.add(Calendar.DATE, 1)` 计算明天。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言抽象类、实现 Serializable/Cloneable/Comparable、关键方法存在。
- 黑盒（固定 `TimeZone("UTC")` + `Locale.US`，不用墙钟）：
  - epoch 字段（含 `DAY_OF_WEEK == Thursday`）。
  - set/get 组合、before/after。
  - `add` 跨年进位 vs `roll` 同年环绕。
  - `getActualMaximum` 闰年 29 / 平年 28 / 四月 30。
  - `setTimeInMillis`/`getTimeInMillis` 往返、clone/equals 组合。
