# javax.sql.rowset.spi.SyncProvider 设计案

## 1. API 功能

`SyncProvider` 是 `RowSet` 同步机制的抽象 SPI 基类。它定义 provider 元数据访问器（providerID/vendor/version）、同步等级、数据源锁级别、可更新视图能力，以及读写器工厂方法。

公开成员（依据 JDK-27 `javap`）：

- 同步等级常量（`getProviderGrade` 返回值域）：
  - `GRADE_NONE = 1`、`GRADE_CHECK_MODIFIED_AT_COMMIT = 2`、`GRADE_CHECK_ALL_AT_COMMIT = 3`、`GRADE_LOCK_WHEN_MODIFIED = 4`、`GRADE_LOCK_WHEN_LOADED = 5`
- 数据源锁常量（`getDataSourceLock`/`setDataSourceLock` 参数域）：
  - `DATASOURCE_NO_LOCK = 1`、`DATASOURCE_ROW_LOCK = 2`、`DATASOURCE_TABLE_LOCK = 3`、`DATASOURCE_DB_LOCK = 4`
- 可更新视图常量（`supportsUpdatableView` 返回值域）：
  - `UPDATABLE_VIEW_SYNC = 5`、`NONUPDATABLE_VIEW_SYNC = 6`
- 抽象方法（9 个）：
  - `getProviderID()`、`getRowSetReader()`、`getRowSetWriter()`、`getProviderGrade()`、`setDataSourceLock(int)`、`getDataSourceLock()`、`supportsUpdatableView()`、`getVersion()`、`getVendor()`

注意：`supportsUpdatableView()` 返回 `int`（不是 boolean）；`setDataSourceLock`/`getDataSourceLock` 声明 `throws SyncProviderException`。

## 2. Java 用户如何使用

用户通常不直接 new，而是通过 `SyncFactory.getInstance(providerID)` 取得实现，或继承该类自定义 provider：

```java
SyncProvider p = SyncFactory.getInstance("com.sun.rowset.providers.RIOptimisticProvider");
int grade = p.getProviderGrade();                 // GRADE_CHECK_MODIFIED_AT_COMMIT
int lock  = p.getDataSourceLock();                // DATASOURCE_NO_LOCK
int view  = p.supportsUpdatableView();            // NONUPDATABLE_VIEW_SYNC
p.setDataSourceLock(SyncProvider.DATASOURCE_NO_LOCK);
```

参考实现 `RIOptimisticProvider` 只接受 `DATASOURCE_NO_LOCK`，其它锁级别会抛 `SyncProviderException`。

## 3. 开源代码实际用法调研

- JDK 自带测试 `test/jdk/javax/sql/testng/util/StubSyncProvider.java`：继承 `SyncProvider` 实现全部抽象方法（返回固定 providerID/vendor/version，其余抛 `UnsupportedOperationException`），用于 `SyncFactoryTests` 的注册/获取往返。
- JDK 实现 `com/sun/rowset/providers/RIOptimisticProvider.java`：真实实现，`getProviderID` 返回 `"com.sun.rowset.providers.RIOptimisticProvider"`、vendor `"Oracle Corporation"`、version `"1.0"`、grade `GRADE_CHECK_MODIFIED_AT_COMMIT`、lock `DATASOURCE_NO_LOCK`、view `NONUPDATABLE_VIEW_SYNC`；`setDataSourceLock` 对非 `NO_LOCK` 抛 `SyncProviderException`。
- JDK 实现 `com/sun/rowset/providers/RIXMLProvider.java`：grade `GRADE_NONE`、lock `DATASOURCE_NO_LOCK`、view `NONUPDATABLE_VIEW_SYNC`。

## 4. 测试用例设计 + 自动化实现

`SyncProviderTest.java` 分两部分：

- **白盒**：抽象类、public、父类 Object、无接口、声明方法数 9、全部 11 个常量精确值。
- **黑盒**：
  - 有状态匿名子类：逐一断言 9 个抽象方法的返回/往返（尤其 `setDataSourceLock` → `getDataSourceLock` 状态保持）。
  - 参考实现行为：`RIOptimisticProvider` 的 grade/lock/view，以及对 `ROW_LOCK`/`TABLE_LOCK` 调用 `setDataSourceLock` 抛 `SyncProviderException`。

自动化断言总数：30 个 check（白盒 + 黑盒）。
