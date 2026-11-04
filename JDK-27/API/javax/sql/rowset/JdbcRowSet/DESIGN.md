# javax.sql.rowset.JdbcRowSet 测试设计

## 1. API 功能
`JdbcRowSet` 是连接型 RowSet 接口，扩展 `RowSet` 与 `Joinable`，实现类 `com.sun.rowset.JdbcRowSetImpl`。接口自身新增 8 个方法：

- `getShowDeleted/setShowDeleted`：删除行可见性。
- `getRowSetWarnings()`：返回 `RowSetWarning`。
- `commit()`、`setAutoCommit/getAutoCommit`、`rollback()`、`rollback(Savepoint)`：事务控制。

其余大量方法（`setUrl/setCommand/setUsername/setPassword`、`setType/getType`、`setTransactionIsolation`、`execute/next/getXxx` 等）继承自 `BaseRowSet`/`RowSet`/`ResultSet`。

## 2. Java 用户如何使用
```java
JdbcRowSet jrs = RowSetProvider.newFactory().createJdbcRowSet();
jrs.setUrl("jdbc:..."); jrs.setUsername("u"); jrs.setPassword("p");
jrs.setCommand("SELECT * FROM T");
jrs.execute();     // 建立连接并执行
while (jrs.next()) { /* getXxx */ }
```
在不连库时（本测试策略）只验证描述性 getter/setter，不调用 `execute/connect`。

## 3. 开源代码实际用法调研
来源 `~/git/jdk25u-dev/test/jdk/javax/sql/testng/test/rowset/jdbcrowset/JdbcRowSetDriverManagerTest.java` 与源码，不凭记忆。

- `JdbcRowSetDriverManagerTest.java`：用 `DriverManager` 连接 Derby 后测 `execute`/游标/更新，属真实连库路径（本测试规避）。
- 源码 `JdbcRowSetImpl.java` 无参构造器：`setReadOnly(true)`、`setTransactionIsolation(TRANSACTION_READ_COMMITTED)`、`setType(TYPE_SCROLL_INSENSITIVE)`、`setConcurrency(CONCUR_UPDATABLE)`，因此这些 getter 的默认值与 `BaseRowSet` 不同。
- 源码 `JdbcRowSetImpl.java`：`getConcurrency()`/`getFetchSize()`/`setFetchSize()`/`commit/rollback/getAutoCommit` 等会访问未建立的 `conn`/`rs`（NPE 或 `checkState` 抛 SQLException），未连接时不能调用；`getType()` 在 `rs == null` 时回退 `super.getType()`（安全）。

## 4. 测试用例设计 + 自动化实现
测试文件 `JdbcRowSetTest.java`：
- 白盒 `testWhiteBox()`：保留原有接口形态断言（接口、父接口 `RowSet`/`Joinable`、声明方法 8）。
- 黑盒 `testBlackBox()`：impl 类名/instanceof；url/command/username/password 往返；`getType/setType` 往返；showDeleted 往返；`getTransactionIsolation` 默认 read-committed 与 set/get；`getRowSetWarnings()==null`；read-only 默认 true 与 set/get。
- 覆盖场景：32 条断言。

自动化要点：严格不调用 `execute/connect/commit/rollback/setAutoCommit/getConcurrency/setFetchSize` 等会触发连接或 NPE 的方法；纯 headless，不加 `@key`。
