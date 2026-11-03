# java.sql.SQLInput 黑盒行为测试设计

## 1. API 功能（接口）
从 `javap -public java.sql.SQLInput` 提取的 public 成员：

  - `public interface java.sql.SQLInput {`
  - `public abstract java.lang.String readString() throws java.sql.SQLException;`
  - `public abstract boolean readBoolean() throws java.sql.SQLException;`
  - `public abstract byte readByte() throws java.sql.SQLException;`
  - `public abstract short readShort() throws java.sql.SQLException;`
  - `public abstract int readInt() throws java.sql.SQLException;`
  - `public abstract long readLong() throws java.sql.SQLException;`
  - `public abstract float readFloat() throws java.sql.SQLException;`
  - `public abstract double readDouble() throws java.sql.SQLException;`
  - `public abstract java.math.BigDecimal readBigDecimal() throws java.sql.SQLException;`
  - `public abstract byte[] readBytes() throws java.sql.SQLException;`
  - `public abstract java.sql.Date readDate() throws java.sql.SQLException;`
  - `public abstract java.sql.Time readTime() throws java.sql.SQLException;`
  - `public abstract java.sql.Timestamp readTimestamp() throws java.sql.SQLException;`
  - `public abstract java.io.Reader readCharacterStream() throws java.sql.SQLException;`
  - `public abstract java.io.InputStream readAsciiStream() throws java.sql.SQLException;`
  - `public abstract java.io.InputStream readBinaryStream() throws java.sql.SQLException;`
  - `public abstract java.lang.Object readObject() throws java.sql.SQLException;`
  - `public abstract java.sql.Ref readRef() throws java.sql.SQLException;`
  - `public abstract java.sql.Blob readBlob() throws java.sql.SQLException;`
  - `public abstract java.sql.Clob readClob() throws java.sql.SQLException;`
  - `public abstract java.sql.Array readArray() throws java.sql.SQLException;`
  - `public abstract boolean wasNull() throws java.sql.SQLException;`
  - `public abstract java.net.URL readURL() throws java.sql.SQLException;`
  - `public abstract java.sql.NClob readNClob() throws java.sql.SQLException;`
  - `public abstract java.lang.String readNString() throws java.sql.SQLException;`
  - `public abstract java.sql.SQLXML readSQLXML() throws java.sql.SQLException;`
  - `public abstract java.sql.RowId readRowId() throws java.sql.SQLException;`
  - `public default <T> T readObject(java.lang.Class<T>) throws java.sql.SQLException;`

## 2. Java 用户如何使用
SQLData 的读/写流：readSQL/writeSQL 中按序 read*/write* 字段。

## 3. 源码实际用法调研（JDK 自身）
- `~/git/jdk25u-dev/src/java.sql/share/classes/java/sql/*.java`：接口与实现源码（行为权威）。
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：`java.sql.Date/Time/Timestamp` 与 `java.time` 桥接方法的随机往返测试。
- `~/git/jdk25u-dev/test/jdk/java/sql/testng/util/*`：JDBC 测试工具类对 `Statement`/`ResultSet`/`Connection` 的驱动化组合调用。

## 4. 测试用例设计 + 自动化实现
- 白盒 `testWhiteBox()`：`Class.forName` + `isInterface`/`isEnum`/修饰符/父类/直接父接口/声明方法数断言。
- 黑盒 `testBlackBox()`：构造/工厂实例化 → 多步组合操作 → 可观察行为断言（含边界与精确异常类型）。
- 确定性：不连数据库、不加载真实驱动、不依赖墙钟；固定输入与 Locale/时区无关断言。
