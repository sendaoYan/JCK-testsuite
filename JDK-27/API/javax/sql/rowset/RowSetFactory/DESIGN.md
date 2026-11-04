# javax.sql.rowset.RowSetFactory 测试设计

## 1. API 功能
`RowSetFactory` 是创建各类 RowSet 的工厂接口（无父接口、无常量），实现类 `com.sun.rowset.RowSetFactoryImpl`。声明 5 个工厂方法：

- `createCachedRowSet()`、`createFilteredRowSet()`、`createJdbcRowSet()`、`createJoinRowSet()`、`createWebRowSet()`。

## 2. Java 用户如何使用
```java
RowSetFactory f = RowSetProvider.newFactory();
CachedRowSet crs = f.createCachedRowSet();
FilteredRowSet frs = f.createFilteredRowSet();
JdbcRowSet jrs = f.createJdbcRowSet();
JoinRowSet jnrs = f.createJoinRowSet();
WebRowSet wrs = f.createWebRowSet();
```

## 3. 开源代码实际用法调研
来源 `~/git/jdk25u-dev/test/jdk/javax/sql/testng/test/rowset/`（各包通用 `rowSetFactory()` 工具）与源码，不凭记忆。

- 各测试的 `rowSetFactory()` 统一 `return RowSetProvider.newFactory();`，再调用对应 `createXxxRowSet()` 获得具体行集。
- 源码 `RowSetFactoryImpl.java`：5 个工厂方法分别 `return new CachedRowSetImpl()/FilteredRowSetImpl()/JdbcRowSetImpl()/JoinRowSetImpl()/WebRowSetImpl()`，返回值即 `com.sun.rowset` 下的实现类实例。

## 4. 测试用例设计 + 自动化实现
测试文件 `RowSetFactoryTest.java`：
- 白盒 `testWhiteBox()`：保留原有接口形态断言（接口、无父接口、声明方法 5、返回类型）。
- 黑盒 `testBlackBox()`：`RowSetProvider.newFactory()` 返回 `RowSetFactoryImpl`；逐个 `create*RowSet()` 断言非 null、正确接口 instanceof、实现类名（CachedRowSetImpl/FilteredRowSetImpl/JdbcRowSetImpl/JoinRowSetImpl/WebRowSetImpl）；每个工厂方法连调两次返回不同实例。
- 覆盖场景：21 条断言。

自动化要点：不调用 `createXxxRowSet` 之外的任何连库方法；纯 headless，不加 `@key`。
