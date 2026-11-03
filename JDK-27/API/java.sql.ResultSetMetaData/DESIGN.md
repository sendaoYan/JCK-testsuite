# java.sql.ResultSetMetaData 黑盒行为测试设计

## 1. API 功能（接口）
从 `javap -public java.sql.ResultSetMetaData` 提取的 public 成员：

  - `public interface java.sql.ResultSetMetaData extends java.sql.Wrapper {`
  - `public static final int columnNoNulls;`
  - `public static final int columnNullable;`
  - `public static final int columnNullableUnknown;`
  - `public abstract int getColumnCount() throws java.sql.SQLException;`
  - `public abstract boolean isAutoIncrement(int) throws java.sql.SQLException;`
  - `public abstract boolean isCaseSensitive(int) throws java.sql.SQLException;`
  - `public abstract boolean isSearchable(int) throws java.sql.SQLException;`
  - `public abstract boolean isCurrency(int) throws java.sql.SQLException;`
  - `public abstract int isNullable(int) throws java.sql.SQLException;`
  - `public abstract boolean isSigned(int) throws java.sql.SQLException;`
  - `public abstract int getColumnDisplaySize(int) throws java.sql.SQLException;`
  - `public abstract java.lang.String getColumnLabel(int) throws java.sql.SQLException;`
  - `public abstract java.lang.String getColumnName(int) throws java.sql.SQLException;`
  - `public abstract java.lang.String getSchemaName(int) throws java.sql.SQLException;`
  - `public abstract int getPrecision(int) throws java.sql.SQLException;`
  - `public abstract int getScale(int) throws java.sql.SQLException;`
  - `public abstract java.lang.String getTableName(int) throws java.sql.SQLException;`
  - `public abstract java.lang.String getCatalogName(int) throws java.sql.SQLException;`
  - `public abstract int getColumnType(int) throws java.sql.SQLException;`
  - `public abstract java.lang.String getColumnTypeName(int) throws java.sql.SQLException;`
  - `public abstract boolean isReadOnly(int) throws java.sql.SQLException;`
  - `public abstract boolean isWritable(int) throws java.sql.SQLException;`
  - `public abstract boolean isDefinitelyWritable(int) throws java.sql.SQLException;`
  - `public abstract java.lang.String getColumnClassName(int) throws java.sql.SQLException;`

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
