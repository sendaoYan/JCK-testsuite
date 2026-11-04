# javax.sql.rowset.spi.SyncResolver 设计案

## 1. API 功能

`SyncResolver` 是同步冲突解析接口，继承 `javax.sql.RowSet`（从而继承 `ResultSet`）。它只暴露冲突行，供应用检查冲突状态、读取冲突值并写回解析值，最后提交。

公开成员（依据 JDK-27 `javap`）：

- 冲突状态常量：
  - `UPDATE_ROW_CONFLICT = 0`
  - `DELETE_ROW_CONFLICT = 1`
  - `INSERT_ROW_CONFLICT = 2`
  - `NO_ROW_CONFLICT = 3`
- 方法（7 个）：
  - `int getStatus()`
  - `Object getConflictValue(int index) throws SQLException`
  - `Object getConflictValue(String columnName) throws SQLException`
  - `void setResolvedValue(int index, Object obj) throws SQLException`
  - `void setResolvedValue(String columnName, Object obj) throws SQLException`
  - `boolean nextConflict() throws SQLException`
  - `boolean previousConflict() throws SQLException`

（注意：常量值依次为 0/1/2/3，不是 1/2/3/0。）

## 2. Java 用户如何使用

通常不直接实现（实现它需补齐 `RowSet`/`ResultSet` 上百个方法），而是从 `SyncProviderException.getSyncResolver()` 取得参考实现：

```java
SyncResolver resolver = new SyncProviderException().getSyncResolver();
while (resolver.nextConflict()) {
    if (resolver.getStatus() == SyncResolver.UPDATE_ROW_CONFLICT) {
        resolver.setResolvedValue(1, resolver.getConflictValue(1));
    }
}
```

## 3. 开源代码实际用法调研

- JDK 自带测试 `test/jdk/javax/sql/testng/util/StubSyncResolver.java`：`implements SyncResolver, Serializable`，补齐 `RowSet`/`ResultSet` 全部方法（大多抛 `UnsupportedOperationException`），作为测试替身。
- JDK 实现 `com/sun/rowset/internal/SyncResolverImpl.java`：参考实现 `extends CachedRowSetImpl implements SyncResolver`；`getStatus()` 在 `stats` 为 null 时返回 `NO_ROW_CONFLICT`，`previousConflict()` 恒返回 false，`setResolvedValue(String,Object)` 恒抛 `SQLException("Method not supported")`。
- JDK 实现 `SyncProviderException.java` 的 `getSyncResolver()` 惰性创建 `SyncResolverImpl`。

## 4. 测试用例设计 + 自动化实现

`SyncResolverTest.java` 分两部分：

- **白盒**：接口形态（interface、public、非 enum/annotation、直接父接口 `javax.sql.RowSet`、声明方法数 7、4 个常量反射取值）。
- **黑盒**：通过 `new SyncProviderException().getSyncResolver()` 取参考实现，断言其是 `RowSet`；常量直接引用值；空解析器的 `getStatus()==NO_ROW_CONFLICT`、`previousConflict()/nextConflict()==false`；`getConflictValue`/`setResolvedValue`（int 与 String 两组）均抛 `SQLException`。

自动化断言总数：25 个 check（白盒 + 黑盒）。
