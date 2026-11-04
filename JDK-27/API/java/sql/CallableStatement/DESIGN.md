# java.sql.CallableStatement 黑盒行为测试设计

## 1. API 功能（接口）
从 `javap -public java.sql.CallableStatement` 提取的 public 成员：

  - `public interface java.sql.CallableStatement extends java.sql.PreparedStatement {`
  - `public abstract void registerOutParameter(int, int) throws java.sql.SQLException;`
  - `public abstract void registerOutParameter(int, int, int) throws java.sql.SQLException;`
  - `public abstract boolean wasNull() throws java.sql.SQLException;`
  - `public abstract java.lang.String getString(int) throws java.sql.SQLException;`
  - `public abstract boolean getBoolean(int) throws java.sql.SQLException;`
  - `public abstract byte getByte(int) throws java.sql.SQLException;`
  - `public abstract short getShort(int) throws java.sql.SQLException;`
  - `public abstract int getInt(int) throws java.sql.SQLException;`
  - `public abstract long getLong(int) throws java.sql.SQLException;`
  - `public abstract float getFloat(int) throws java.sql.SQLException;`
  - `public abstract double getDouble(int) throws java.sql.SQLException;`
  - `public abstract java.math.BigDecimal getBigDecimal(int, int) throws java.sql.SQLException;`
  - `public abstract byte[] getBytes(int) throws java.sql.SQLException;`
  - `public abstract java.sql.Date getDate(int) throws java.sql.SQLException;`
  - `public abstract java.sql.Time getTime(int) throws java.sql.SQLException;`
  - `public abstract java.sql.Timestamp getTimestamp(int) throws java.sql.SQLException;`
  - `public abstract java.lang.Object getObject(int) throws java.sql.SQLException;`
  - `public abstract java.math.BigDecimal getBigDecimal(int) throws java.sql.SQLException;`
  - `public abstract java.lang.Object getObject(int, java.util.Map<java.lang.String, java.lang.Class<?>>) throws java.sql.SQLException;`
  - `public abstract java.sql.Ref getRef(int) throws java.sql.SQLException;`
  - `public abstract java.sql.Blob getBlob(int) throws java.sql.SQLException;`
  - `public abstract java.sql.Clob getClob(int) throws java.sql.SQLException;`
  - `public abstract java.sql.Array getArray(int) throws java.sql.SQLException;`
  - `public abstract java.sql.Date getDate(int, java.util.Calendar) throws java.sql.SQLException;`
  - `public abstract java.sql.Time getTime(int, java.util.Calendar) throws java.sql.SQLException;`
  - `public abstract java.sql.Timestamp getTimestamp(int, java.util.Calendar) throws java.sql.SQLException;`
  - `public abstract void registerOutParameter(int, int, java.lang.String) throws java.sql.SQLException;`
  - `public abstract void registerOutParameter(java.lang.String, int) throws java.sql.SQLException;`
  - `public abstract void registerOutParameter(java.lang.String, int, int) throws java.sql.SQLException;`
  - `public abstract void registerOutParameter(java.lang.String, int, java.lang.String) throws java.sql.SQLException;`
  - `public abstract java.net.URL getURL(int) throws java.sql.SQLException;`
  - `public abstract void setURL(java.lang.String, java.net.URL) throws java.sql.SQLException;`
  - `public abstract void setNull(java.lang.String, int) throws java.sql.SQLException;`
  - `public abstract void setBoolean(java.lang.String, boolean) throws java.sql.SQLException;`
  - `public abstract void setByte(java.lang.String, byte) throws java.sql.SQLException;`
  - `public abstract void setShort(java.lang.String, short) throws java.sql.SQLException;`
  - `public abstract void setInt(java.lang.String, int) throws java.sql.SQLException;`
  - `public abstract void setLong(java.lang.String, long) throws java.sql.SQLException;`
  - `public abstract void setFloat(java.lang.String, float) throws java.sql.SQLException;`
  - ... (共 122 个 public 成员)

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
