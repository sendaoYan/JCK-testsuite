# java.sql.Statement 黑盒行为测试设计

## 1. API 功能（接口）
从 `javap -public java.sql.Statement` 提取的 public 成员：

  - `public interface java.sql.Statement extends java.sql.Wrapper,java.lang.AutoCloseable {`
  - `public static final int CLOSE_CURRENT_RESULT;`
  - `public static final int KEEP_CURRENT_RESULT;`
  - `public static final int CLOSE_ALL_RESULTS;`
  - `public static final int SUCCESS_NO_INFO;`
  - `public static final int EXECUTE_FAILED;`
  - `public static final int RETURN_GENERATED_KEYS;`
  - `public static final int NO_GENERATED_KEYS;`
  - `public abstract java.sql.ResultSet executeQuery(java.lang.String) throws java.sql.SQLException;`
  - `public abstract int executeUpdate(java.lang.String) throws java.sql.SQLException;`
  - `public abstract void close() throws java.sql.SQLException;`
  - `public abstract int getMaxFieldSize() throws java.sql.SQLException;`
  - `public abstract void setMaxFieldSize(int) throws java.sql.SQLException;`
  - `public abstract int getMaxRows() throws java.sql.SQLException;`
  - `public abstract void setMaxRows(int) throws java.sql.SQLException;`
  - `public abstract void setEscapeProcessing(boolean) throws java.sql.SQLException;`
  - `public abstract int getQueryTimeout() throws java.sql.SQLException;`
  - `public abstract void setQueryTimeout(int) throws java.sql.SQLException;`
  - `public abstract void cancel() throws java.sql.SQLException;`
  - `public abstract java.sql.SQLWarning getWarnings() throws java.sql.SQLException;`
  - `public abstract void clearWarnings() throws java.sql.SQLException;`
  - `public abstract void setCursorName(java.lang.String) throws java.sql.SQLException;`
  - `public abstract boolean execute(java.lang.String) throws java.sql.SQLException;`
  - `public abstract java.sql.ResultSet getResultSet() throws java.sql.SQLException;`
  - `public abstract int getUpdateCount() throws java.sql.SQLException;`
  - `public abstract boolean getMoreResults() throws java.sql.SQLException;`
  - `public abstract void setFetchDirection(int) throws java.sql.SQLException;`
  - `public abstract int getFetchDirection() throws java.sql.SQLException;`
  - `public abstract void setFetchSize(int) throws java.sql.SQLException;`
  - `public abstract int getFetchSize() throws java.sql.SQLException;`
  - `public abstract int getResultSetConcurrency() throws java.sql.SQLException;`
  - `public abstract int getResultSetType() throws java.sql.SQLException;`
  - `public abstract void addBatch(java.lang.String) throws java.sql.SQLException;`
  - `public abstract void clearBatch() throws java.sql.SQLException;`
  - `public abstract int[] executeBatch() throws java.sql.SQLException;`
  - `public abstract java.sql.Connection getConnection() throws java.sql.SQLException;`
  - `public abstract boolean getMoreResults(int) throws java.sql.SQLException;`
  - `public abstract java.sql.ResultSet getGeneratedKeys() throws java.sql.SQLException;`
  - `public abstract int executeUpdate(java.lang.String, int) throws java.sql.SQLException;`
  - `public abstract int executeUpdate(java.lang.String, int[]) throws java.sql.SQLException;`
  - ... (共 62 个 public 成员)

## 2. Java 用户如何使用
JDBC 接口：由驱动/连接池供应商实现；用户面向接口编程（`Connection`/`Statement`/`ResultSet`/`DataSource` 等），反射断言其形态并调用默认方法。

## 3. 源码实际用法调研（JDK 自身）
- `~/git/jdk25u-dev/src/java.sql/share/classes/java/sql/*.java`：接口与实现源码（行为权威）。
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：`java.sql.Date/Time/Timestamp` 与 `java.time` 桥接方法的随机往返测试。
- `~/git/jdk25u-dev/test/jdk/java/sql/testng/util/*`：JDBC 测试工具类对 `Statement`/`ResultSet`/`Connection` 的驱动化组合调用。

## 4. 测试用例设计 + 自动化实现
- 白盒 `testWhiteBox()`：`Class.forName` + `isInterface`/`isEnum`/修饰符/父类/直接父接口/声明方法数断言。
- 黑盒 `testBlackBox()`：构造/工厂实例化 → 多步组合操作 → 可观察行为断言（含边界与精确异常类型）。
- 确定性：不连数据库、不加载真实驱动、不依赖墙钟；固定输入与 Locale/时区无关断言。
