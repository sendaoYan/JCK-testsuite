# java.sql.SQLXML 黑盒行为测试设计

## 1. API 功能（接口）
从 `javap -public java.sql.SQLXML` 提取的 public 成员：

  - `public interface java.sql.SQLXML extends java.lang.AutoCloseable {`
  - `public abstract void free() throws java.sql.SQLException;`
  - `public abstract java.io.InputStream getBinaryStream() throws java.sql.SQLException;`
  - `public abstract java.io.OutputStream setBinaryStream() throws java.sql.SQLException;`
  - `public abstract java.io.Reader getCharacterStream() throws java.sql.SQLException;`
  - `public abstract java.io.Writer setCharacterStream() throws java.sql.SQLException;`
  - `public abstract java.lang.String getString() throws java.sql.SQLException;`
  - `public abstract void setString(java.lang.String) throws java.sql.SQLException;`
  - `public abstract <T extends javax.xml.transform.Source> T getSource(java.lang.Class<T>) throws java.sql.SQLException;`
  - `public abstract <T extends javax.xml.transform.Result> T setResult(java.lang.Class<T>) throws java.sql.SQLException;`
  - `public default void close() throws java.sql.SQLException;`

## 2. Java 用户如何使用
JDBC 接口：由驱动/连接池供应商实现；用户面向接口编程（`Connection`/`Statement`/`ResultSet`/`DataSource` 等），反射断言其形态并调用默认方法。

## 3. 源码实际用法调研（JDK 自身）
- `~/git/jdk25u-dev/src/java.sql/share/classes/java/sql/*.java`：接口与实现源码（行为权威）。
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：`java.sql.Date/Time/Timestamp` 与 `java.time` 桥接方法的随机往返测试。
- `~/git/jdk25u-dev/test/jdk/java/sql/testng/util/*`：JDBC 测试工具类对 `Statement`/`ResultSet`/`Connection` 的驱动化组合调用。

## 4. 测试用例设计 + 自动化实现
- 白盒 `testWhiteBox()`：`Class.forName` + `isInterface`/`isEnum`/修饰符/父类/直接父接口/声明方法数断言。
- 黑盒 `testBlackBox()`：`Proxy` 生成 `SQLXML` 实例（`getString` 返回哨兵 XML 字符串，其余 void/流/`Source`/`Result` 返回 null 形状），以接口类型 `invokeinterface` 调用全部 10 个方法并断言形状/哨兵，最后断言 10 次全部分派。
- 确定性：不连数据库、不加载真实驱动、不依赖墙钟；固定输入与 Locale/时区无关断言。
