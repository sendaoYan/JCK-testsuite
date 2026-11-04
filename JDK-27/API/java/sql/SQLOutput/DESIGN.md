# java.sql.SQLOutput 黑盒行为测试设计

## 1. API 功能（接口）
从 `javap -public java.sql.SQLOutput` 提取的 public 成员：

  - `public interface java.sql.SQLOutput {`
  - `public abstract void writeString(java.lang.String) throws java.sql.SQLException;`
  - `public abstract void writeBoolean(boolean) throws java.sql.SQLException;`
  - `public abstract void writeByte(byte) throws java.sql.SQLException;`
  - `public abstract void writeShort(short) throws java.sql.SQLException;`
  - `public abstract void writeInt(int) throws java.sql.SQLException;`
  - `public abstract void writeLong(long) throws java.sql.SQLException;`
  - `public abstract void writeFloat(float) throws java.sql.SQLException;`
  - `public abstract void writeDouble(double) throws java.sql.SQLException;`
  - `public abstract void writeBigDecimal(java.math.BigDecimal) throws java.sql.SQLException;`
  - `public abstract void writeBytes(byte[]) throws java.sql.SQLException;`
  - `public abstract void writeDate(java.sql.Date) throws java.sql.SQLException;`
  - `public abstract void writeTime(java.sql.Time) throws java.sql.SQLException;`
  - `public abstract void writeTimestamp(java.sql.Timestamp) throws java.sql.SQLException;`
  - `public abstract void writeCharacterStream(java.io.Reader) throws java.sql.SQLException;`
  - `public abstract void writeAsciiStream(java.io.InputStream) throws java.sql.SQLException;`
  - `public abstract void writeBinaryStream(java.io.InputStream) throws java.sql.SQLException;`
  - `public abstract void writeObject(java.sql.SQLData) throws java.sql.SQLException;`
  - `public abstract void writeRef(java.sql.Ref) throws java.sql.SQLException;`
  - `public abstract void writeBlob(java.sql.Blob) throws java.sql.SQLException;`
  - `public abstract void writeClob(java.sql.Clob) throws java.sql.SQLException;`
  - `public abstract void writeStruct(java.sql.Struct) throws java.sql.SQLException;`
  - `public abstract void writeArray(java.sql.Array) throws java.sql.SQLException;`
  - `public abstract void writeURL(java.net.URL) throws java.sql.SQLException;`
  - `public abstract void writeNString(java.lang.String) throws java.sql.SQLException;`
  - `public abstract void writeNClob(java.sql.NClob) throws java.sql.SQLException;`
  - `public abstract void writeRowId(java.sql.RowId) throws java.sql.SQLException;`
  - `public abstract void writeSQLXML(java.sql.SQLXML) throws java.sql.SQLException;`
  - `public default void writeObject(java.lang.Object, java.sql.SQLType) throws java.sql.SQLException;`

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
