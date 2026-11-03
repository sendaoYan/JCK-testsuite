# javax.sql.rowset.Joinable 测试设计

## 1. API 功能
`Joinable` 是标记「可参与 JoinRowSet 连接」的接口（无常量、无父接口），声明 10 个匹配列方法：

- 设置：`setMatchColumn(int)`、`setMatchColumn(int[])`、`setMatchColumn(String)`、`setMatchColumn(String[])`。
- 查询：`getMatchColumnIndexes()`、`getMatchColumnNames()`。
- 取消：`unsetMatchColumn(int)`、`unsetMatchColumn(int[])`、`unsetMatchColumn(String)`、`unsetMatchColumn(String[])`。

`CachedRowSet`、`JdbcRowSet` 均继承该接口，实际由 `com.sun.rowset.CachedRowSetImpl`/`JdbcRowSetImpl` 实现。

## 2. Java 用户如何使用
```java
Joinable j = (Joinable) RowSetProvider.newFactory().createCachedRowSet();
j.setMatchColumn("ID");       // 或 setMatchColumn(2)
int[] idx = j.getMatchColumnIndexes();
String[] names = j.getMatchColumnNames();
j.unsetMatchColumn(2);
```

## 3. 开源代码实际用法调研
来源 `~/git/jdk25u-dev/test/jdk/javax/sql/testng/test/rowset/joinrowset/JoinRowSetTests.java` 与源码，不凭记忆。

- `JoinRowSetTests.java`：通过 `addRowSet(crs, "SUP_ID")` 内部调用 `CachedRowSetImpl.setMatchColumn(columnName)`，再走 `getMatchColumnIndexes/getMatchColumnNames` 读取匹配列。
- 源码 `CachedRowSetImpl.java`：匹配列存储 `iMatchColumns`（初始 10 个 -1）与 `strMatchColumns`（初始 10 个 null）；未设置时 `getMatchColumnIndexes/getMatchColumnNames` 抛 SQLException；`setMatchColumn(int[])`/`setMatchColumn(String[])` 用 `Vector.add(i, v)`（累加式），因此只断言前 N 个元素而非整数组相等；`setMatchColumn(-1)`/空串/null 抛 SQLException。

## 4. 测试用例设计 + 自动化实现
测试文件 `JoinableTest.java`：
- 白盒 `testWhiteBox()`：保留原有接口形态断言（接口、无父接口、声明方法 10）。
- 黑盒 `testBlackBox()`：匿名实现 `Joinable` 验证接口可实例化及 `set/get/unset` 契约；再用真实 `CachedRowSetImpl`（Joinable 实现）验证：未设置时 getter 抛 SQLException、`setMatchColumn(int/String/int[]/String[])` 往返、非法值抛 SQLException、`unsetMatchColumn` 后 getter 再次抛 SQLException。
- 覆盖场景：22 条断言。

自动化要点：`int[]/String[]` 场景用独立新实例并只断言前两个元素（规避 impl 累加式存储）；纯内存不连库；纯 headless，不加 `@key`。
