# java.sql.Connection 黑盒行为测试设计

## 1. API 功能（接口）
从 `javap -public java.sql.Connection` 提取的 public 成员：

  - `public interface java.sql.Connection extends java.sql.Wrapper,java.lang.AutoCloseable {`
  - `public static final int TRANSACTION_NONE;`
  - `public static final int TRANSACTION_READ_UNCOMMITTED;`
  - `public static final int TRANSACTION_READ_COMMITTED;`
  - `public static final int TRANSACTION_REPEATABLE_READ;`
  - `public static final int TRANSACTION_SERIALIZABLE;`
  - `public abstract java.sql.Statement createStatement() throws java.sql.SQLException;`
  - `public abstract java.sql.PreparedStatement prepareStatement(java.lang.String) throws java.sql.SQLException;`
  - `public abstract java.sql.CallableStatement prepareCall(java.lang.String) throws java.sql.SQLException;`
  - `public abstract java.lang.String nativeSQL(java.lang.String) throws java.sql.SQLException;`
  - `public abstract void setAutoCommit(boolean) throws java.sql.SQLException;`
  - `public abstract boolean getAutoCommit() throws java.sql.SQLException;`
  - `public abstract void commit() throws java.sql.SQLException;`
  - `public abstract void rollback() throws java.sql.SQLException;`
  - `public abstract void close() throws java.sql.SQLException;`
  - `public abstract boolean isClosed() throws java.sql.SQLException;`
  - `public abstract java.sql.DatabaseMetaData getMetaData() throws java.sql.SQLException;`
  - `public abstract void setReadOnly(boolean) throws java.sql.SQLException;`
  - `public abstract boolean isReadOnly() throws java.sql.SQLException;`
  - `public abstract void setCatalog(java.lang.String) throws java.sql.SQLException;`
  - `public abstract java.lang.String getCatalog() throws java.sql.SQLException;`
  - `public abstract void setTransactionIsolation(int) throws java.sql.SQLException;`
  - `public abstract int getTransactionIsolation() throws java.sql.SQLException;`
  - `public abstract java.sql.SQLWarning getWarnings() throws java.sql.SQLException;`
  - `public abstract void clearWarnings() throws java.sql.SQLException;`
  - `public abstract java.sql.Statement createStatement(int, int) throws java.sql.SQLException;`
  - `public abstract java.sql.PreparedStatement prepareStatement(java.lang.String, int, int) throws java.sql.SQLException;`
  - `public abstract java.sql.CallableStatement prepareCall(java.lang.String, int, int) throws java.sql.SQLException;`
  - `public abstract java.util.Map<java.lang.String, java.lang.Class<?>> getTypeMap() throws java.sql.SQLException;`
  - `public abstract void setTypeMap(java.util.Map<java.lang.String, java.lang.Class<?>>) throws java.sql.SQLException;`
  - `public abstract void setHoldability(int) throws java.sql.SQLException;`
  - `public abstract int getHoldability() throws java.sql.SQLException;`
  - `public abstract java.sql.Savepoint setSavepoint() throws java.sql.SQLException;`
  - `public abstract java.sql.Savepoint setSavepoint(java.lang.String) throws java.sql.SQLException;`
  - `public abstract void rollback(java.sql.Savepoint) throws java.sql.SQLException;`
  - `public abstract void releaseSavepoint(java.sql.Savepoint) throws java.sql.SQLException;`
  - `public abstract java.sql.Statement createStatement(int, int, int) throws java.sql.SQLException;`
  - `public abstract java.sql.PreparedStatement prepareStatement(java.lang.String, int, int, int) throws java.sql.SQLException;`
  - `public abstract java.sql.CallableStatement prepareCall(java.lang.String, int, int, int) throws java.sql.SQLException;`
  - `public abstract java.sql.PreparedStatement prepareStatement(java.lang.String, int) throws java.sql.SQLException;`
  - ... (共 68 个 public 成员)

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
