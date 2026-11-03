# java.sql.DriverManager 黑盒行为测试设计

## 1. API 功能（类）
从 `javap -public java.sql.DriverManager` 提取的 public 成员：

  - `public class java.sql.DriverManager {`
  - `public static java.io.PrintWriter getLogWriter();`
  - `public static void setLogWriter(java.io.PrintWriter);`
  - `public static java.sql.Connection getConnection(java.lang.String, java.util.Properties) throws java.sql.SQLException;`
  - `public static java.sql.Connection getConnection(java.lang.String, java.lang.String, java.lang.String) throws java.sql.SQLException;`
  - `public static java.sql.Connection getConnection(java.lang.String) throws java.sql.SQLException;`
  - `public static java.sql.Driver getDriver(java.lang.String) throws java.sql.SQLException;`
  - `public static void registerDriver(java.sql.Driver) throws java.sql.SQLException;`
  - `public static void registerDriver(java.sql.Driver, java.sql.DriverAction) throws java.sql.SQLException;`
  - `public static void deregisterDriver(java.sql.Driver) throws java.sql.SQLException;`
  - `public static java.util.Enumeration<java.sql.Driver> getDrivers();`
  - `public static java.util.stream.Stream<java.sql.Driver> drivers();`
  - `public static void setLoginTimeout(int);`
  - `public static int getLoginTimeout();`
  - `public static void setLogStream(java.io.PrintStream);`
  - `public static java.io.PrintStream getLogStream();`
  - `public static void println(java.lang.String);`

## 2. Java 用户如何使用
传统 JDBC 入口：`DriverManager.getConnection(url, user, pass)` 建连，`registerDriver`/`deregisterDriver` 管理驱动，`setLoginTimeout`/`setLogWriter` 配置。

## 3. 源码实际用法调研（JDK 自身）
- `~/git/jdk25u-dev/src/java.sql/share/classes/java/sql/*.java`：接口与实现源码（行为权威）。
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：`java.sql.Date/Time/Timestamp` 与 `java.time` 桥接方法的随机往返测试。
- `~/git/jdk25u-dev/test/jdk/java/sql/testng/util/*`：JDBC 测试工具类对 `Statement`/`ResultSet`/`Connection` 的驱动化组合调用。

## 4. 测试用例设计 + 自动化实现
- 白盒 `testWhiteBox()`：`Class.forName` + `isInterface`/`isEnum`/修饰符/父类/直接父接口/声明方法数断言。
- 黑盒 `testBlackBox()`：构造/工厂实例化 → 多步组合操作 → 可观察行为断言（含边界与精确异常类型）。
- 确定性：不连数据库、不加载真实驱动、不依赖墙钟；固定输入与 Locale/时区无关断言。
