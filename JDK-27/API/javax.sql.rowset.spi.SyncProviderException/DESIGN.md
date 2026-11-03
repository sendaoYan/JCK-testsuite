# javax.sql.rowset.spi.SyncProviderException 设计案

## 1. API 功能

`SyncProviderException` 是 `SyncProvider` 在读取/写回数据源过程中检测到冲突或违反约束时抛出的异常，继承 `java.sql.SQLException`，并携带一个 `SyncResolver`（冲突解析器）。

公开成员（依据 JDK-27 `javap`）：

- 构造器：
  - `SyncProviderException()`
  - `SyncProviderException(String msg)`
  - `SyncProviderException(SyncResolver syncResolver)` —— 参数为 `null` 抛 `IllegalArgumentException`
- 方法：
  - `SyncResolver getSyncResolver()` —— 若未显式设置，惰性创建默认实现 `com.sun.rowset.internal.SyncResolverImpl`
  - `void setSyncResolver(SyncResolver)` —— 参数为 `null` 抛 `IllegalArgumentException`

（注意：**没有** `(String, SyncResolver)` 构造器。）

## 2. Java 用户如何使用

```java
try {
    rowSet.acceptChanges();
} catch (SyncProviderException e) {
    SyncResolver resolver = e.getSyncResolver();   // 默认解析器
    while (resolver.nextConflict()) {
        if (resolver.getStatus() == SyncResolver.UPDATE_ROW_CONFLICT) {
            Object v = resolver.getConflictValue(1);
            resolver.setResolvedValue(1, v);
        }
    }
}
```

## 3. 开源代码实际用法调研

- JDK 自带测试 `test/jdk/javax/sql/testng/test/rowset/spi/SyncProviderExceptionTests.java`：
  - 无参/带消息构造后 `getMessage`/`getSQLState`/`getCause`/`getErrorCode` 默认值，且 `getSyncResolver()` 为 `SyncResolverImpl`；
  - `setSyncResolver(new StubSyncResolver())` 后 `getSyncResolver()` 返回该 stub；`new SyncProviderException(new StubSyncResolver())` 直接携带 resolver；
  - 后续测试覆盖 `new SyncProviderException((SyncResolver)null)` 与 `setSyncResolver(null)` 抛 `IllegalArgumentException`。
- JDK 实现 `com/sun/rowset/internal/CachedRowSetWriter.java`：检测到冲突时构造并抛出 `SyncProviderException`，随附 `SyncResolver`。
- JDK 实现 `SyncProviderException.java`：`getSyncResolver()` 对 null 字段惰性 new `SyncResolverImpl`。

## 4. 测试用例设计 + 自动化实现

`SyncProviderExceptionTest.java` 分两部分：

- **白盒**：类形态（类、public、父类 SQLException、声明构造器数 3、声明方法数 2）。
- **黑盒**：
  - 无参/String 构造默认值，`getSyncResolver` 返回非 null 的 `SyncResolver`。
  - 默认解析器行为（无冲突）：`getStatus()==NO_ROW_CONFLICT`、`previousConflict()/nextConflict()==false`、`getConflictValue`/`setResolvedValue` 抛 `SQLException`。
  - `SyncResolver` 构造与 `setSyncResolver` 往返；`null` 参数抛 `IllegalArgumentException`。
  - SQLException 继承链路 `initCause`/`getCause`。

自动化断言总数：25 个 check（白盒 + 黑盒）。
