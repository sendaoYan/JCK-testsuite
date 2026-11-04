# java.sql.ClientInfoStatus 黑盒行为测试设计

## 1. API 功能（枚举）
从 `javap -public java.sql.ClientInfoStatus` 提取的 public 成员：

  - `public final class java.sql.ClientInfoStatus extends java.lang.Enum<java.sql.ClientInfoStatus> {`
  - `public static final java.sql.ClientInfoStatus REASON_UNKNOWN;`
  - `public static final java.sql.ClientInfoStatus REASON_UNKNOWN_PROPERTY;`
  - `public static final java.sql.ClientInfoStatus REASON_VALUE_INVALID;`
  - `public static final java.sql.ClientInfoStatus REASON_VALUE_TRUNCATED;`
  - `public static java.sql.ClientInfoStatus[] values();`
  - `public static java.sql.ClientInfoStatus valueOf(java.lang.String);`

## 2. Java 用户如何使用
枚举：`values()`/`valueOf(String)` 遍历与反查，`name()` 取标识符；用于 JDBC 元数据返回值（如 RowIdLifetime、PseudoColumnUsage）。

## 3. 源码实际用法调研（JDK 自身）
- `~/git/jdk25u-dev/src/java.sql/share/classes/java/sql/*.java`：接口与实现源码（行为权威）。
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：`java.sql.Date/Time/Timestamp` 与 `java.time` 桥接方法的随机往返测试。
- `~/git/jdk25u-dev/test/jdk/java/sql/testng/util/*`：JDBC 测试工具类对 `Statement`/`ResultSet`/`Connection` 的驱动化组合调用。

## 4. 测试用例设计 + 自动化实现
- 白盒 `testWhiteBox()`：`Class.forName` + `isInterface`/`isEnum`/修饰符/父类/直接父接口/声明方法数断言。
- 黑盒 `testBlackBox()`：构造/工厂实例化 → 多步组合操作 → 可观察行为断言（含边界与精确异常类型）。
- 确定性：不连数据库、不加载真实驱动、不依赖墙钟；固定输入与 Locale/时区无关断言。
