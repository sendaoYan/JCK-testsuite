# java.sql.JDBCType 黑盒行为测试设计

## 1. API 功能（枚举）
从 `javap -public java.sql.JDBCType` 提取的 public 成员：

  - `public final class java.sql.JDBCType extends java.lang.Enum<java.sql.JDBCType> implements java.sql.SQLType {`
  - `public static final java.sql.JDBCType BIT;`
  - `public static final java.sql.JDBCType TINYINT;`
  - `public static final java.sql.JDBCType SMALLINT;`
  - `public static final java.sql.JDBCType INTEGER;`
  - `public static final java.sql.JDBCType BIGINT;`
  - `public static final java.sql.JDBCType FLOAT;`
  - `public static final java.sql.JDBCType REAL;`
  - `public static final java.sql.JDBCType DOUBLE;`
  - `public static final java.sql.JDBCType NUMERIC;`
  - `public static final java.sql.JDBCType DECIMAL;`
  - `public static final java.sql.JDBCType CHAR;`
  - `public static final java.sql.JDBCType VARCHAR;`
  - `public static final java.sql.JDBCType LONGVARCHAR;`
  - `public static final java.sql.JDBCType DATE;`
  - `public static final java.sql.JDBCType TIME;`
  - `public static final java.sql.JDBCType TIMESTAMP;`
  - `public static final java.sql.JDBCType BINARY;`
  - `public static final java.sql.JDBCType VARBINARY;`
  - `public static final java.sql.JDBCType LONGVARBINARY;`
  - `public static final java.sql.JDBCType NULL;`
  - `public static final java.sql.JDBCType OTHER;`
  - `public static final java.sql.JDBCType JAVA_OBJECT;`
  - `public static final java.sql.JDBCType DISTINCT;`
  - `public static final java.sql.JDBCType STRUCT;`
  - `public static final java.sql.JDBCType ARRAY;`
  - `public static final java.sql.JDBCType BLOB;`
  - `public static final java.sql.JDBCType CLOB;`
  - `public static final java.sql.JDBCType REF;`
  - `public static final java.sql.JDBCType DATALINK;`
  - `public static final java.sql.JDBCType BOOLEAN;`
  - `public static final java.sql.JDBCType ROWID;`
  - `public static final java.sql.JDBCType NCHAR;`
  - `public static final java.sql.JDBCType NVARCHAR;`
  - `public static final java.sql.JDBCType LONGNVARCHAR;`
  - `public static final java.sql.JDBCType NCLOB;`
  - `public static final java.sql.JDBCType SQLXML;`
  - `public static final java.sql.JDBCType REF_CURSOR;`
  - `public static final java.sql.JDBCType TIME_WITH_TIMEZONE;`
  - `public static final java.sql.JDBCType TIMESTAMP_WITH_TIMEZONE;`
  - ... (共 48 个 public 成员)

## 2. Java 用户如何使用
JDBC 4.2 后推荐用 `JDBCType.INTEGER` 等枚举代替 `Types` 常量传给 `setObject`/`registerOutParameter`，并用 `getVendorTypeNumber()` 映射回 `Types` 值。

## 3. 源码实际用法调研（JDK 自身）
- `~/git/jdk25u-dev/src/java.sql/share/classes/java/sql/*.java`：接口与实现源码（行为权威）。
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：`java.sql.Date/Time/Timestamp` 与 `java.time` 桥接方法的随机往返测试。
- `~/git/jdk25u-dev/test/jdk/java/sql/testng/util/*`：JDBC 测试工具类对 `Statement`/`ResultSet`/`Connection` 的驱动化组合调用。

## 4. 测试用例设计 + 自动化实现
- 白盒 `testWhiteBox()`：`Class.forName` + `isInterface`/`isEnum`/修饰符/父类/直接父接口/声明方法数断言。
- 黑盒 `testBlackBox()`：构造/工厂实例化 → 多步组合操作 → 可观察行为断言（含边界与精确异常类型）。
- 确定性：不连数据库、不加载真实驱动、不依赖墙钟；固定输入与 Locale/时区无关断言。
