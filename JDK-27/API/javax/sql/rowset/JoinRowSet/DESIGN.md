# javax.sql.rowset.JoinRowSet 测试设计

## 1. API 功能
`JoinRowSet` 扩展 `WebRowSet`，把多个 `Joinable` RowSet 按匹配列做 SQL 风格连接。实现类 `com.sun.rowset.JoinRowSetImpl`。接口方法：

- `addRowSet(Joinable)`、`addRowSet(RowSet,int)`、`addRowSet(RowSet,String)`、`addRowSet(RowSet[],int[])`、`addRowSet(RowSet[],String[])`：加入参与连接的行集。
- `getRowSets()`、`getRowSetNames()`：查询已加入的行集/表名。
- `toCachedRowSet()`：把连接结果转成 `CachedRowSet`。
- `supportsCrossJoin/InnerJoin/LeftOuterJoin/RightOuterJoin/FullJoin`、`setJoinType/getJoinType`、`getWhereClause`。
- 常量：`CROSS_JOIN=0`、`INNER_JOIN=1`、`LEFT_OUTER_JOIN=2`、`RIGHT_OUTER_JOIN=3`、`FULL_JOIN=4`。

## 2. Java 用户如何使用
```java
JoinRowSet jrs = RowSetProvider.newFactory().createJoinRowSet();
jrs.setJoinType(JoinRowSet.INNER_JOIN);
jrs.addRowSet(crsA, "ID");   // 以 ID 为匹配列
jrs.addRowSet(crsB, "ID");
jrs.beforeFirst();
while (jrs.next()) { /* 读取连接结果行 */ }
CachedRowSet flat = jrs.toCachedRowSet();
```

## 3. 开源代码实际用法调研
来源 `~/git/jdk25u-dev/test/jdk/javax/sql/testng/test/rowset/joinrowset/JoinRowSetTests.java` 与源码，不凭记忆。

- `JoinRowSetTests.java`：用两个 `CachedRowSet`（COFFEES/SUPPLIERS）分别 `addRowSet(crs, "SUP_ID")` 与 `addRowSet(crs1, "SUP_ID")` 后，遍历 `jrs.next()` 并按列名 `getInt("COF_ID")`/`getInt("SUP_ID")` 校验 inner join 结果。
- 源码 `JoinRowSetImpl.java`：`supportedJOINs = {false,true,false,false,false}`，即仅支持 inner join；`setJoinType` 对非 INNER_JOIN 抛 SQLException；`initJOIN` 把结果列组织为「左表全部列 + 右表非匹配列」，连接结果行序不保证与插入序一致；`getJoinType` 在未显式 setJoinType 时会因空 vector 出问题，需先 `setJoinType(INNER_JOIN)`。

## 4. 测试用例设计 + 自动化实现
测试文件 `JoinRowSetTest.java`：
- 白盒 `testWhiteBox()`：保留原有接口形态断言（接口、父接口 `WebRowSet`、声明方法 16、5 个 join 常量值）。
- 黑盒 `testBlackBox()`：impl 类名/instanceof；`supports*Join` 真值；`setJoinType(CROSS_JOIN)` 抛 SQLException、`setJoinType(INNER_JOIN)` 成功；两个内存 `CachedRowSet` 同列名 `ID` 内连接后 `size==2`、`getRowSets().size()==2`、`getRowSetNames()`、`getWhereClause()` 含 where；连接结果按 ID 排序后校验 (2,A2,B2)/(3,A3,B3)；结果元数据 3 列；`toCachedRowSet()` size 2。
- 覆盖场景：35 条断言。

自动化要点：连接结果行序不确定，断言前先按 ID 排序；`getJoinType` 前必须先 `setJoinType(INNER_JOIN)`（规避 impl 空 vector 问题）；纯内存不连库；纯 headless，不加 `@key`。
