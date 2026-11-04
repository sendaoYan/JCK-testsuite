# java.sql.DatabaseMetaData 黑盒行为测试设计

## 1. API 功能（接口）
从 `javap -public java.sql.DatabaseMetaData` 提取的 public 成员：

  - `public interface java.sql.DatabaseMetaData extends java.sql.Wrapper {`
  - `public static final int procedureResultUnknown;`
  - `public static final int procedureNoResult;`
  - `public static final int procedureReturnsResult;`
  - `public static final int procedureColumnUnknown;`
  - `public static final int procedureColumnIn;`
  - `public static final int procedureColumnInOut;`
  - `public static final int procedureColumnOut;`
  - `public static final int procedureColumnReturn;`
  - `public static final int procedureColumnResult;`
  - `public static final int procedureNoNulls;`
  - `public static final int procedureNullable;`
  - `public static final int procedureNullableUnknown;`
  - `public static final int columnNoNulls;`
  - `public static final int columnNullable;`
  - `public static final int columnNullableUnknown;`
  - `public static final int bestRowTemporary;`
  - `public static final int bestRowTransaction;`
  - `public static final int bestRowSession;`
  - `public static final int bestRowUnknown;`
  - `public static final int bestRowNotPseudo;`
  - `public static final int bestRowPseudo;`
  - `public static final int versionColumnUnknown;`
  - `public static final int versionColumnNotPseudo;`
  - `public static final int versionColumnPseudo;`
  - `public static final int importedKeyCascade;`
  - `public static final int importedKeyRestrict;`
  - `public static final int importedKeySetNull;`
  - `public static final int importedKeyNoAction;`
  - `public static final int importedKeySetDefault;`
  - `public static final int importedKeyInitiallyDeferred;`
  - `public static final int importedKeyInitiallyImmediate;`
  - `public static final int importedKeyNotDeferrable;`
  - `public static final int typeNoNulls;`
  - `public static final int typeNullable;`
  - `public static final int typeNullableUnknown;`
  - `public static final int typePredNone;`
  - `public static final int typePredChar;`
  - `public static final int typePredBasic;`
  - `public static final int typeSearchable;`
  - ... (共 239 个 public 成员)

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
