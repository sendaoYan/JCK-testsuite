# java.sql.Array 黑盒行为测试设计

## 1. API 功能（接口）
从 `javap -public java.sql.Array` 提取的 public 成员：

  - `public interface java.sql.Array extends java.lang.AutoCloseable {`
  - `public abstract java.lang.String getBaseTypeName() throws java.sql.SQLException;`
  - `public abstract int getBaseType() throws java.sql.SQLException;`
  - `public abstract java.lang.Object getArray() throws java.sql.SQLException;`
  - `public abstract java.lang.Object getArray(java.util.Map<java.lang.String, java.lang.Class<?>>) throws java.sql.SQLException;`
  - `public abstract java.lang.Object getArray(long, int) throws java.sql.SQLException;`
  - `public abstract java.lang.Object getArray(long, int, java.util.Map<java.lang.String, java.lang.Class<?>>) throws java.sql.SQLException;`
  - `public abstract java.sql.ResultSet getResultSet() throws java.sql.SQLException;`
  - `public abstract java.sql.ResultSet getResultSet(java.util.Map<java.lang.String, java.lang.Class<?>>) throws java.sql.SQLException;`
  - `public abstract java.sql.ResultSet getResultSet(long, int) throws java.sql.SQLException;`
  - `public abstract java.sql.ResultSet getResultSet(long, int, java.util.Map<java.lang.String, java.lang.Class<?>>) throws java.sql.SQLException;`
  - `public abstract void free() throws java.sql.SQLException;`
  - `public default void close() throws java.sql.SQLException;`

## 2. Java 用户如何使用
轻量接口：Array/Struct/Ref 表示 SQL 结构化类型，RowId 表示行标识，SQLData 用户自定义类型映射，Savepoint 事务保存点，Wrapper 解包供应商实现。

## 3. 源码实际用法调研（JDK 自身）
- `~/git/jdk25u-dev/src/java.sql/share/classes/java/sql/*.java`：接口与实现源码（行为权威）。
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：`java.sql.Date/Time/Timestamp` 与 `java.time` 桥接方法的随机往返测试。
- `~/git/jdk25u-dev/test/jdk/java/sql/testng/util/*`：JDBC 测试工具类对 `Statement`/`ResultSet`/`Connection` 的驱动化组合调用。

## 4. 测试用例设计 + 自动化实现
- 白盒 `testWhiteBox()`：`Class.forName` + `isInterface`/`isEnum`/修饰符/父类/直接父接口/声明方法数断言。
- 黑盒 `testBlackBox()`：JDK-27 的 `SerialArray` 只有 `SerialArray(Array[,Map])` 构造器（无 `Object[]` 构造器），故先用 Proxy 构造一个返回 `Object[]{...}`/`Types.JAVA_OBJECT`/`"java.lang.Object"` 的源 `Array`，再包成 `SerialArray`；全程以 `java.sql.Array` 接口类型 `invokeinterface` 调用全部 12 个方法：`getBaseType/getBaseTypeName/getArray(3 个重载)/getResultSet(4 个重载, SerialArray 规定抛 SerialException)/free/close`。
- 确定性：不连数据库、不加载真实驱动、不依赖墙钟；固定输入与 Locale/时区无关断言。
