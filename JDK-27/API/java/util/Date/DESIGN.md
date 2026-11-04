# java.util.Date 测试设计

## 1. API 功能
- 构造器：`Date()`、`Date(long)`（epoch 毫秒）、`Date(int,int,int)` 及 5/6 参（弃用）、`Date(String)`（弃用）。
- 时间读写：`getTime()`、`setTime(long)`。
- 比较/相等：`before`、`after`、`equals`、`compareTo`、`hashCode`。
- 复制/转换：`clone()`、`toInstant()`、`from(Instant)`。
- 静态工具：`UTC(int,int,int,int,int,int)`、`parse(String)`。

## 2. Java 用户如何使用
- 时间点构造：`new Date(System.currentTimeMillis())` / `new Date(0)`。
- 比较链路：`if (start.before(end) && end.after(start))`；`d1.compareTo(d2) < 0`。
- 持久化时间戳：`long ms = d.getTime()` → 存库 → `new Date(ms)` 还原。
- 与 java.time 互转：`Date.from(instant)` / `d.toInstant()`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/net/URLConnection.java`：`getLastModified()` 返回 `Date`，用 `setLastModified(long)` 内部 `new Date`。
- JDK 源码 `java/net/HttpURLConnection.java`：`getDate()` 解析 HTTP `Date` 头返回 `Date`。
- JDK 源码 `java/text/MessageFormat.java`：内部用 `Date` 做格式化参数类型分派。
- 广泛用法（Spring/MyBatis）：实体属性类型 `java.util.Date` 与 `getTime()` 毫秒值互转。

## 4. 测试用例设计 + 自动化实现
- 白盒（`DateTest.testWhiteBox()`）：`Class.forName` 断言类非接口/非抽象、继承 Object、实现 Serializable/Cloneable/Comparable，关键方法存在。
- 黑盒（`DateTest.testBlackBox()`）：
  - epoch 与 setTime/getTime/equals/hashCode 组合。
  - compareTo/before/after 符号与传递链。
  - clone 不同对象但值相等。
  - `Date.UTC` 固定 epoch 边界、`toInstant`/`from` 往返。
  - `parse` 非法输入抛 IllegalArgumentException（不依赖默认时区，`parse` 只断异常类型）。
