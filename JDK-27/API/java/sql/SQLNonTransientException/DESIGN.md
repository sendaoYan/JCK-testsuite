# java.sql.SQLNonTransientException 黑盒行为测试设计

## 1. API 功能（类）
从 `javap -public java.sql.SQLNonTransientException` 提取的 public 成员：

  - `public class java.sql.SQLNonTransientException extends java.sql.SQLException {`
  - `public java.sql.SQLNonTransientException();`
  - `public java.sql.SQLNonTransientException(java.lang.String);`
  - `public java.sql.SQLNonTransientException(java.lang.String, java.lang.String);`
  - `public java.sql.SQLNonTransientException(java.lang.String, java.lang.String, int);`
  - `public java.sql.SQLNonTransientException(java.lang.Throwable);`
  - `public java.sql.SQLNonTransientException(java.lang.String, java.lang.Throwable);`
  - `public java.sql.SQLNonTransientException(java.lang.String, java.lang.String, java.lang.Throwable);`
  - `public java.sql.SQLNonTransientException(java.lang.String, java.lang.String, int, java.lang.Throwable);`

## 2. Java 用户如何使用
SQLException 分类子类：按 SQL 状态抛出/捕获，`instanceof` 区分瞬态/非瞬态，构造器透传 message/SQLState/errorCode/cause。

## 3. 源码实际用法调研（JDK 自身）
- `~/git/jdk25u-dev/src/java.sql/share/classes/java/sql/*.java`：接口与实现源码（行为权威）。
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：`java.sql.Date/Time/Timestamp` 与 `java.time` 桥接方法的随机往返测试。
- `~/git/jdk25u-dev/test/jdk/java/sql/testng/util/*`：JDBC 测试工具类对 `Statement`/`ResultSet`/`Connection` 的驱动化组合调用。

## 4. 测试用例设计 + 自动化实现
- 白盒 `testWhiteBox()`：`Class.forName` + `isInterface`/`isEnum`/修饰符/父类/直接父接口/声明方法数断言。
- 黑盒 `testBlackBox()`：构造/工厂实例化 → 多步组合操作 → 可观察行为断言（含边界与精确异常类型）。
- 确定性：不连数据库、不加载真实驱动、不依赖墙钟；固定输入与 Locale/时区无关断言。
