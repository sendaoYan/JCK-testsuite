# javax.sql.rowset.BaseRowSet 测试设计

## 1. API 功能
`BaseRowSet` 是所有 RowSet 实现（`CachedRowSet`、`JdbcRowSet`）的抽象基类，本身无抽象方法、可直接匿名实例化，实现 `Serializable` 与 `Cloneable`。主要功能分组：

- 连接描述属性：`setUrl/getUrl`、`setDataSourceName/getDataSourceName`、`setUsername/getUsername`、`setPassword/getPassword`、`setCommand/getCommand`。
- 结果集特征：`setType/getType`、`setConcurrency/getConcurrency`、`setReadOnly/isReadOnly`、`setFetchDirection/getFetchDirection`、`setFetchSize/getFetchSize`。
- 事务/查询限制：`setTransactionIsolation/getTransactionIsolation`、`setTypeMap/getTypeMap`、`setMaxFieldSize/getMaxFieldSize`、`setMaxRows/getMaxRows`、`setQueryTimeout/getQueryTimeout`、`setEscapeProcessing/getEscapeProcessing`、`setShowDeleted/getShowDeleted`。
- 参数绑定：大量 `setXxx(int,String)` 与 `setNull`/流式 setter，以及 `clearParameters/getParams`。
- 常量：`UNICODE_STREAM_PARAM=0`、`BINARY_STREAM_PARAM=1`、`ASCII_STREAM_PARAM=2`。

## 2. Java 用户如何使用
用户通常不直接使用 `BaseRowSet`，而是由子类实现（如 `com.sun.rowset.CachedRowSetImpl`）继承这些公共属性。典型链路：

```java
RowSet rs = ...;                 // 具体实现
rs.setUrl("jdbc:..."); rs.setUsername("u"); rs.setPassword("p");
rs.setCommand("SELECT ...");
rs.setType(ResultSet.TYPE_SCROLL_INSENSITIVE);
rs.setConcurrency(ResultSet.CONCUR_UPDATABLE);
rs.execute();                    // 子类实现，触发连接/读取
```
不连库时也可直接验证属性：`setUrl → getUrl`、`setType → getType`、`setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED) → getTransactionIsolation`、`setString(1,"x") → getParams()` 等组合往返。

## 3. 开源代码实际用法调研
来源优先 `~/git/jdk25u-dev/test/jdk/javax/sql/testng/test/rowset` 与 JDK 源码，不凭记忆。

- JDK 自带测试 `CommonRowSetTests.java`：在抽象基类测试中通过 `RowSetProvider.newFactory()` 创建具体 RowSet，用 DataProvider 覆盖 fetch direction、concurrency、type 等属性 set/get 往返。
- `BaseRowSetTests.java`：用 `StubBaseRowSet` 子类测 `addRowSetListener/removeRowSetListener` 与 `notifyCursorMoved/notifyRowChanged/notifyRowSetChanged`（事件通知）。
- JDK 源码 `BaseRowSet.java`：`setCommand` 在 `params == null` 时抛 `SQLException("Set initParams() before setCommand")`；`setUrl("")`/`setDataSourceName("")` 抛 SQLException；`setMaxRows` 在 `max < fetchSize` 时抛 SQLException；`setTransactionIsolation` 只接受 5 个 `Connection.TRANSACTION_*` 常量。
- 上述行为是本测试 `BaseRowSetTest.java` 黑盒断言的权威依据。

## 4. 测试用例设计 + 自动化实现
测试文件 `BaseRowSetTest.java`：
- 白盒 `testWhiteBox()`：保留原有反射断言（抽象类、public、父类 Object、2 个接口、声明方法数 130、3 个流常量），补构造器形态断言。
- 黑盒 `testBlackBox()`：先用 `getParams()` 初始化参数存储（规避 `setCommand` 对 `initParams()` 的依赖），再覆盖：
  - 凭证/命令/URL/数据源名互斥（`setDataSourceName` 清空 url，`setUrl` 清空 dataSourceName）及空串异常。
  - type/concurrency/transaction isolation 的默认值、set/get 往返与非法值异常。
  - typeMap、maxFieldSize、maxRows、queryTimeout、escapeProcessing、fetchDirection、fetchSize、showDeleted 往返与负值异常。
  - 参数存储：`setString(1,"x")`+`setInt(2,42)` → `getParams()` 长度与取值，`clearParameters()` 清空。
- 覆盖场景：59 条断言（含多步组合操作与异常路径）。

自动化实现要点：固定使用匿名 `new BaseRowSet() {}`；异常断言只断精确类型（`SQLException`）；不连数据库、纯 headless，不加 `@key`。
