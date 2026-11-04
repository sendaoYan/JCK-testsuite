# java.sql.Timestamp 黑盒行为测试设计

## 1. API 功能（类）
从 `javap -public java.sql.Timestamp` 提取的 public 成员：

  - `public class java.sql.Timestamp extends java.util.Date {`
  - `public java.sql.Timestamp(int, int, int, int, int, int, int);`
  - `public java.sql.Timestamp(long);`
  - `public void setTime(long);`
  - `public long getTime();`
  - `public static java.sql.Timestamp valueOf(java.lang.String);`
  - `public java.lang.String toString();`
  - `public int getNanos();`
  - `public void setNanos(int);`
  - `public boolean equals(java.sql.Timestamp);`
  - `public boolean equals(java.lang.Object);`
  - `public boolean before(java.sql.Timestamp);`
  - `public boolean after(java.sql.Timestamp);`
  - `public int compareTo(java.sql.Timestamp);`
  - `public int compareTo(java.util.Date);`
  - `public int hashCode();`
  - `public static java.sql.Timestamp valueOf(java.time.LocalDateTime);`
  - `public java.time.LocalDateTime toLocalDateTime();`
  - `public static java.sql.Timestamp from(java.time.Instant);`
  - `public java.time.Instant toInstant();`
  - `public int compareTo(java.lang.Object);`

## 2. Java 用户如何使用
用户通过 `valueOf(String)` / `valueOf(LocalDate|LocalTime|LocalDateTime)` / `from(Instant)` 构造，再用 `toString`/`toLocalDate`/`toLocalTime`/`toLocalDateTime`/`toInstant` 与 java.time 互转；用 `getTime`/`setTime`/`compareTo`/`before`/`after`/`equals` 比较与存储。

## 3. 源码实际用法调研（JDK 自身）
- `~/git/jdk25u-dev/src/java.sql/share/classes/java/sql/*.java`：接口与实现源码（行为权威）。
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：`java.sql.Date/Time/Timestamp` 与 `java.time` 桥接方法的随机往返测试。
- `~/git/jdk25u-dev/test/jdk/java/sql/testng/util/*`：JDBC 测试工具类对 `Statement`/`ResultSet`/`Connection` 的驱动化组合调用。

## 4. 测试用例设计 + 自动化实现
- 白盒 `testWhiteBox()`：`Class.forName` + `isInterface`/`isEnum`/修饰符/父类/直接父接口/声明方法数断言。
- 黑盒 `testBlackBox()`：构造/工厂实例化 → 多步组合操作 → 可观察行为断言（含边界与精确异常类型）。
- 确定性：不连数据库、不加载真实驱动、不依赖墙钟；固定输入与 Locale/时区无关断言。
