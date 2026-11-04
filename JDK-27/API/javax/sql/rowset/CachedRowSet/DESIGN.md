# javax.sql.rowset.CachedRowSet 测试设计

## 1. API 功能
`CachedRowSet` 是断连型可滚动结果集接口，扩展 `javax.sql.RowSet` 与 `Joinable`，实现类为 `com.sun.rowset.CachedRowSetImpl`。核心分组：

- 数据装载/同步：`populate(ResultSet)`、`execute(Connection)`、`acceptChanges()`、`restoreOriginal()`、`release()`。
- 撤销：`undoDelete/undoInsert/undoUpdate`。
- 修改追踪：`columnUpdated(int/String)`、`rowUpdated/rowInserted/rowDeleted`。
- 集合转换：`toCollection()`/`toCollection(int)`/`toCollection(String)`。
- 副本/视图：`createShared()`、`createCopy()`、`createCopySchema()`、`createCopyNoConstraints()`。
- 元数据/键/表：`setMetaData/getMetaData`、`setKeyColumns/getKeyColumns`、`setTableName/getTableName`。
- 分页：`setPageSize/getPageSize`、`nextPage/previousPage`。
- 游标：继承 `ResultSet` 的 `beforeFirst/first/last/absolute/relative/previous/next`、`getRow/isBeforeFirst/isAfterLast/isFirst/isLast`。
- 常量：`COMMIT_ON_ACCEPT_CHANGES=true`。

## 2. Java 用户如何使用
内存填充（不连库）是 JDK 自带测试的通用模式：
```java
RowSetFactory f = RowSetProvider.newFactory();
CachedRowSet crs = f.createCachedRowSet();
RowSetMetaDataImpl md = new RowSetMetaDataImpl();
md.setColumnCount(2); md.setColumnName(1,"ID"); md.setColumnType(1,Types.INTEGER);
md.setColumnName(2,"NAME"); md.setColumnType(2,Types.VARCHAR);
crs.setMetaData(md);
crs.moveToInsertRow(); crs.updateInt(1,1); crs.updateString(2,"Alice"); crs.insertRow();
crs.moveToCurrentRow(); crs.beforeFirst();
while (crs.next()) { crs.getInt(1); crs.getString(2); }
```
组合链路：`setMetaData → moveToInsertRow → update* → insertRow → moveToCurrentRow → 游标移动 → getXxx → toCollection/createCopy`。

## 3. 开源代码实际用法调研
来源 `~/git/jdk25u-dev/test/jdk/javax/sql/testng/test/rowset/cachedrowset/CommonCachedRowSetTests.java` 与 JDK 源码，不凭记忆。

- `CommonCachedRowSetTests.java`：用 `createCachedRowSet` + Derby 表填充，验证 `size()`、`absolute`、`beforeFirst/isBeforeFirst/first/isFirst`、`afterLast/isAfterLast/last/isLast` 游标语义，以及 `undoInsert`（插入后撤销）、`undoDelete`（setShowDeleted(true) 后撤销删除）、`undoUpdate`（插入行撤销）等。
- 源码 `CachedRowSetImpl.java`：`insertRow()` 把新行插到 `currentRow` 位置（beforeFirst 时插到第 0 位）；`setMetaData` 在 0 行时会把 `readOnly` 置为 true；`toCollection(int)` 内部 `createCopy` 后靠游标 `next()` 读取，因此调用前需 `beforeFirst()`。
- 上述行为是黑盒断言与规避策略的依据。

## 4. 测试用例设计 + 自动化实现
测试文件 `CachedRowSetTest.java`：
- 白盒 `testWhiteBox()`：保留原有接口形态断言（接口、父接口 `RowSet`/`Joinable`、声明方法 41、`COMMIT_ON_ACCEPT_CHANGES`）。
- 黑盒 `testBlackBox()`：`createCachedRowSet` 的 impl 类名/instanceof；内存填 3 行后 `size`、顺序迭代、`getMetaData` 列名/类型、`findColumn`；游标移动（beforeFirst/first/last/absolute/relative/getRow/afterLast/previous）；`toCollection(1)`/`toCollection("NAME")`；`setTableName/setKeyColumns/setPageSize` 与异常；`createCopy/createCopySchema/createCopyNoConstraints/createShared`；`undoInsert`/`undoUpdate`（插入行撤销）。
- 覆盖场景：73 条断言。

自动化要点：内存填充不连库；`toCollection` 前 `beforeFirst()`；`undoInsert` 用 `first()` 定位插入行（插入位置在 currentRow 处而非固定末尾）；纯 headless，不加 `@key`。
