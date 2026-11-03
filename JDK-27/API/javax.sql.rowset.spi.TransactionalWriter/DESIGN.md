# javax.sql.rowset.spi.TransactionalWriter 设计案

## 1. API 功能

`TransactionalWriter` 是事务性写入接口，继承 `javax.sql.RowSetWriter`（从而继承 `boolean writeData(RowSetInternal)`），为 `RowSet` 写入器补充显式提交/回滚语义。

公开成员（依据 JDK-27 `javap`）：

- 继承自 `RowSetWriter`：`boolean writeData(RowSetInternal caller) throws SQLException`
- 本接口新增（3 个）：
  - `void commit() throws SQLException`
  - `void rollback() throws SQLException`
  - `void rollback(Savepoint s) throws SQLException`

（注意：本接口**没有**任何常量字段；直接父接口是 `javax.sql.RowSetWriter`，不是 `XmlWriter`。）

## 2. Java 用户如何使用

自定义写入器若需要事务控制，可实现本接口：

```java
TransactionalWriter w = new TransactionalWriter() {
    public boolean writeData(RowSetInternal caller) { return true; }
    public void commit() { /* con.commit(); */ }
    public void rollback() { /* con.rollback(); */ }
    public void rollback(Savepoint s) { /* con.rollback(s); */ }
};
w.commit();
w.rollback(savepoint);
```

## 3. 开源代码实际用法调研

- JDK 实现 `com/sun/rowset/internal/CachedRowSetWriter.java`：`public class CachedRowSetWriter implements TransactionalWriter, Serializable`，是参考写入器；`commit()` 调用 `con.commit()`，`rollback()` 调用 `con.rollback()`，`rollback(Savepoint)` 调用 `con.rollback(s)`；`CachedRowSetImpl` 与 `CachedRowSetWriter` 是唯一引用该接口的 JDK 实现处。
- 该接口在 `test/jdk` 中没有直接单测文件，属于较冷门的 SPI 扩展点。

## 4. 测试用例设计 + 自动化实现

`TransactionalWriterTest.java` 分两部分：

- **白盒**：接口形态（interface、public、非 enum/annotation、直接父接口 `javax.sql.RowSetWriter`、声明方法数 3）。
- **黑盒**：有状态匿名实现，组合操作断言：
  - `instanceof RowSetWriter`；
  - `writeData(null)` 返回 true 且被调用；
  - `commit()` / `rollback()` 计数递增；
  - 构造匿名 `Savepoint`（id/name），`rollback(savepoint)` 计数递增且收到同一 savepoint 实例。

自动化断言总数：17 个 check（白盒 + 黑盒）。
