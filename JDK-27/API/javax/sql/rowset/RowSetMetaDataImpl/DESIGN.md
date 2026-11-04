# javax.sql.rowset.RowSetMetaDataImpl 测试设计

## 1. API 功能
`RowSetMetaDataImpl` 是 `RowSetMetaData` 的具体实现类，实现 `javax.sql.RowSetMetaData` 与 `java.io.Serializable`，提供无参构造器。它把元数据（列数、列名、列类型、可空性、精度、小数位、是否自增/只读/可写等）存于内部 `Vector`，`setColumnCount(n)` 后即可按 1 基列索引 `setXxx/getXxx`。

## 2. Java 用户如何使用
```java
RowSetMetaDataImpl md = new RowSetMetaDataImpl();
md.setColumnCount(2);
md.setColumnName(1, "ID"); md.setColumnType(1, Types.INTEGER);
md.setColumnName(2, "NAME"); md.setColumnType(2, Types.VARCHAR);
crs.setMetaData(md);   // 供 CachedRowSet 内存填充
```

## 3. 开源代码实际用法调研
来源 `~/git/jdk25u-dev/test/jdk/javax/sql/testng/test/rowset/cachedrowset/CommonCachedRowSetTests.java` 与源码，不凭记忆。

- `CommonCachedRowSetTests.java` 用 `CachedRowSetImpl.setMetaData(...)` 内建元数据后插入行，验证 `getMetaData()` 的 `getColumnCount/getColumnName/getColumnType` 等。
- 源码 `RowSetMetaDataImpl.java`：未 `setColumnCount` 前访问列元数据会因 `col < 1 || col > colCount`（colCount=0）抛 SQLException；`getColumnCount/setColumnCount` 支持往返；各 `getXxx` 有列号越界校验。

## 4. 测试用例设计 + 自动化实现
测试文件 `RowSetMetaDataImplTest.java`：
- 白盒 `testWhiteBox()`：反射断言（具体类、实现 `RowSetMetaData`+`Serializable`、声明构造器 1 个）。
- 黑盒 `testBlackBox()`：初始 `getColumnCount()==0`、越界访问抛 SQLException；`setColumnCount(3)` 后默认列名/类型、逐列 `set/getColumnName`、`set/getColumnType`、`isAutoIncrement/isCaseSensitive/isNullable/isReadOnly/isWritable/isSigned/isSearchable/isCurrency/isDefinitelyWritable` 往返、`getColumnLabel/getCatalogName/getSchemaName/getTableName/getColumnClassName/getColumnDisplaySize/getPrecision/getScale/getColumnTypeName`；非法列号抛 SQLException。
- 覆盖场景：42 条断言。

自动化要点：无 DB 依赖，纯内存元数据；纯 headless，不加 `@key`。
