# java.sql.PreparedStatement 黑盒行为测试设计

## 1. API 功能（接口）
从 `javap -public java.sql.PreparedStatement` 提取的 public 成员：

  - `public interface java.sql.PreparedStatement extends java.sql.Statement {`
  - `public abstract java.sql.ResultSet executeQuery() throws java.sql.SQLException;`
  - `public abstract int executeUpdate() throws java.sql.SQLException;`
  - `public abstract void setNull(int, int) throws java.sql.SQLException;`
  - `public abstract void setBoolean(int, boolean) throws java.sql.SQLException;`
  - `public abstract void setByte(int, byte) throws java.sql.SQLException;`
  - `public abstract void setShort(int, short) throws java.sql.SQLException;`
  - `public abstract void setInt(int, int) throws java.sql.SQLException;`
  - `public abstract void setLong(int, long) throws java.sql.SQLException;`
  - `public abstract void setFloat(int, float) throws java.sql.SQLException;`
  - `public abstract void setDouble(int, double) throws java.sql.SQLException;`
  - `public abstract void setBigDecimal(int, java.math.BigDecimal) throws java.sql.SQLException;`
  - `public abstract void setString(int, java.lang.String) throws java.sql.SQLException;`
  - `public abstract void setBytes(int, byte[]) throws java.sql.SQLException;`
  - `public abstract void setDate(int, java.sql.Date) throws java.sql.SQLException;`
  - `public abstract void setTime(int, java.sql.Time) throws java.sql.SQLException;`
  - `public abstract void setTimestamp(int, java.sql.Timestamp) throws java.sql.SQLException;`
  - `public abstract void setAsciiStream(int, java.io.InputStream, int) throws java.sql.SQLException;`
  - `public abstract void setUnicodeStream(int, java.io.InputStream, int) throws java.sql.SQLException;`
  - `public abstract void setBinaryStream(int, java.io.InputStream, int) throws java.sql.SQLException;`
  - `public abstract void clearParameters() throws java.sql.SQLException;`
  - `public abstract void setObject(int, java.lang.Object, int) throws java.sql.SQLException;`
  - `public abstract void setObject(int, java.lang.Object) throws java.sql.SQLException;`
  - `public abstract boolean execute() throws java.sql.SQLException;`
  - `public abstract void addBatch() throws java.sql.SQLException;`
  - `public abstract void setCharacterStream(int, java.io.Reader, int) throws java.sql.SQLException;`
  - `public abstract void setRef(int, java.sql.Ref) throws java.sql.SQLException;`
  - `public abstract void setBlob(int, java.sql.Blob) throws java.sql.SQLException;`
  - `public abstract void setClob(int, java.sql.Clob) throws java.sql.SQLException;`
  - `public abstract void setArray(int, java.sql.Array) throws java.sql.SQLException;`
  - `public abstract java.sql.ResultSetMetaData getMetaData() throws java.sql.SQLException;`
  - `public abstract void setDate(int, java.sql.Date, java.util.Calendar) throws java.sql.SQLException;`
  - `public abstract void setTime(int, java.sql.Time, java.util.Calendar) throws java.sql.SQLException;`
  - `public abstract void setTimestamp(int, java.sql.Timestamp, java.util.Calendar) throws java.sql.SQLException;`
  - `public abstract void setNull(int, int, java.lang.String) throws java.sql.SQLException;`
  - `public abstract void setURL(int, java.net.URL) throws java.sql.SQLException;`
  - `public abstract java.sql.ParameterMetaData getParameterMetaData() throws java.sql.SQLException;`
  - `public abstract void setRowId(int, java.sql.RowId) throws java.sql.SQLException;`
  - `public abstract void setNString(int, java.lang.String) throws java.sql.SQLException;`
  - `public abstract void setNCharacterStream(int, java.io.Reader, long) throws java.sql.SQLException;`
  - ... (共 59 个 public 成员)

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
