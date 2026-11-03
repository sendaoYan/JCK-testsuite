# java.sql.Types 黑盒行为测试设计

## 1. API 功能（类）
从 `javap -public java.sql.Types` 提取的 public 成员：

  - `public class java.sql.Types {`
  - `public static final int BIT;`
  - `public static final int TINYINT;`
  - `public static final int SMALLINT;`
  - `public static final int INTEGER;`
  - `public static final int BIGINT;`
  - `public static final int FLOAT;`
  - `public static final int REAL;`
  - `public static final int DOUBLE;`
  - `public static final int NUMERIC;`
  - `public static final int DECIMAL;`
  - `public static final int CHAR;`
  - `public static final int VARCHAR;`
  - `public static final int LONGVARCHAR;`
  - `public static final int DATE;`
  - `public static final int TIME;`
  - `public static final int TIMESTAMP;`
  - `public static final int BINARY;`
  - `public static final int VARBINARY;`
  - `public static final int LONGVARBINARY;`
  - `public static final int NULL;`
  - `public static final int OTHER;`
  - `public static final int JAVA_OBJECT;`
  - `public static final int DISTINCT;`
  - `public static final int STRUCT;`
  - `public static final int ARRAY;`
  - `public static final int BLOB;`
  - `public static final int CLOB;`
  - `public static final int REF;`
  - `public static final int DATALINK;`
  - `public static final int BOOLEAN;`
  - `public static final int ROWID;`
  - `public static final int NCHAR;`
  - `public static final int NVARCHAR;`
  - `public static final int LONGNVARCHAR;`
  - `public static final int NCLOB;`
  - `public static final int SQLXML;`
  - `public static final int REF_CURSOR;`
  - `public static final int TIME_WITH_TIMEZONE;`
  - `public static final int TIMESTAMP_WITH_TIMEZONE;`
  - ... (共 42 个 public 成员)

## 2. Java 用户如何使用
用户把 `java.sql.Types` 的 int 常量传给 JDBC API（如 `setNull(int, Types.VARCHAR)`）标明 SQL 类型。

## 3. 源码实际用法调研（JDK 自身）
- `~/git/jdk25u-dev/src/java.sql/share/classes/java/sql/*.java`：接口与实现源码（行为权威）。
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：`java.sql.Date/Time/Timestamp` 与 `java.time` 桥接方法的随机往返测试。
- `~/git/jdk25u-dev/test/jdk/java/sql/testng/util/*`：JDBC 测试工具类对 `Statement`/`ResultSet`/`Connection` 的驱动化组合调用。

## 4. 测试用例设计 + 自动化实现
- 白盒 `testWhiteBox()`：`Class.forName` + `isInterface`/`isEnum`/修饰符/父类/直接父接口/声明方法数断言。
- 黑盒 `testBlackBox()`：构造/工厂实例化 → 多步组合操作 → 可观察行为断言（含边界与精确异常类型）。
- 确定性：不连数据库、不加载真实驱动、不依赖墙钟；固定输入与 Locale/时区无关断言。
