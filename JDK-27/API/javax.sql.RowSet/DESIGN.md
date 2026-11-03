# javax.sql.RowSet 黑盒行为测试设计

## 1. API 功能（接口）
从 `javap -public javax.sql.RowSet` 提取的 public 成员：

  - `public interface javax.sql.RowSet extends java.sql.ResultSet {`
  - `public abstract java.lang.String getUrl() throws java.sql.SQLException;`
  - `public abstract void setUrl(java.lang.String) throws java.sql.SQLException;`
  - `public abstract java.lang.String getDataSourceName();`
  - `public abstract void setDataSourceName(java.lang.String) throws java.sql.SQLException;`
  - `public abstract java.lang.String getUsername();`
  - `public abstract void setUsername(java.lang.String) throws java.sql.SQLException;`
  - `public abstract java.lang.String getPassword();`
  - `public abstract void setPassword(java.lang.String) throws java.sql.SQLException;`
  - `public abstract int getTransactionIsolation();`
  - `public abstract void setTransactionIsolation(int) throws java.sql.SQLException;`
  - `public abstract java.util.Map<java.lang.String, java.lang.Class<?>> getTypeMap() throws java.sql.SQLException;`
  - `public abstract void setTypeMap(java.util.Map<java.lang.String, java.lang.Class<?>>) throws java.sql.SQLException;`
  - `public abstract java.lang.String getCommand();`
  - `public abstract void setCommand(java.lang.String) throws java.sql.SQLException;`
  - `public abstract boolean isReadOnly();`
  - `public abstract void setReadOnly(boolean) throws java.sql.SQLException;`
  - `public abstract int getMaxFieldSize() throws java.sql.SQLException;`
  - `public abstract void setMaxFieldSize(int) throws java.sql.SQLException;`
  - `public abstract int getMaxRows() throws java.sql.SQLException;`
  - `public abstract void setMaxRows(int) throws java.sql.SQLException;`
  - `public abstract boolean getEscapeProcessing() throws java.sql.SQLException;`
  - `public abstract void setEscapeProcessing(boolean) throws java.sql.SQLException;`
  - `public abstract int getQueryTimeout() throws java.sql.SQLException;`
  - `public abstract void setQueryTimeout(int) throws java.sql.SQLException;`
  - `public abstract void setType(int) throws java.sql.SQLException;`
  - `public abstract void setConcurrency(int) throws java.sql.SQLException;`
  - `public abstract void setNull(int, int) throws java.sql.SQLException;`
  - `public abstract void setNull(java.lang.String, int) throws java.sql.SQLException;`
  - `public abstract void setNull(int, int, java.lang.String) throws java.sql.SQLException;`
  - `public abstract void setNull(java.lang.String, int, java.lang.String) throws java.sql.SQLException;`
  - `public abstract void setBoolean(int, boolean) throws java.sql.SQLException;`
  - `public abstract void setBoolean(java.lang.String, boolean) throws java.sql.SQLException;`
  - `public abstract void setByte(int, byte) throws java.sql.SQLException;`
  - `public abstract void setByte(java.lang.String, byte) throws java.sql.SQLException;`
  - `public abstract void setShort(int, short) throws java.sql.SQLException;`
  - `public abstract void setShort(java.lang.String, short) throws java.sql.SQLException;`
  - `public abstract void setInt(int, int) throws java.sql.SQLException;`
  - `public abstract void setInt(java.lang.String, int) throws java.sql.SQLException;`
  - `public abstract void setLong(int, long) throws java.sql.SQLException;`
  - ... (共 116 个 public 成员)

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
