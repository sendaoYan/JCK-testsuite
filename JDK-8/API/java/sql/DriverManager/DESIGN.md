# java.sql.DriverManager 测试设计案（JDK-8）

> 完整契约：静态属性 round-trip、注册/注销（含 DriverAction）、getConnection 精确异常与
> 匿名假 Driver + 动态代理 Connection（**不加载真实驱动、不真连库**）。

## 1. API 提供功能
- 静态属性：`set/getLoginTimeout`、`set/getLogWriter`、`set/getLogStream`（deprecated）、`println`。
- 注册：`registerDriver(Driver)`、`registerDriver(Driver,DriverAction)`（null → NPE）；`deregisterDriver(Driver)`（null → 无操作，触发 DriverAction.deregister()）。
- 连接/定位：`getConnection(url[,user,password][,info])`、`getDriver(url)`、`getDrivers()`。

## 2. 使用方式
`registerDriver` 注册 → `getConnection` 连接 → `getDriver` 定位 → `getDrivers` 枚举 → `deregisterDriver` 注销。

## 3. 参考来源
- javadoc `java/sql/DriverManager.html`；源码 `~/git/jdk8u-dev/.../java/sql/DriverManager.java`；JCK `api/java_sql/DriverManager`。

## 4. 测试用例设计
| 测试点 | 用例方法 |
|---|---|
| loginTimeout round-trip（0/42/-1 无校验/MAX） | testLoginTimeout |
| logWriter/logStream round-trip（含相互联动） | testLogWriterStream |
| println 无抛（null writer no-op） | testPrintln |
| register/deregister + null → NPE / deregister(null) 无操作 | testRegisterDeregister |
| registerDriver(driver,action) + deregister 触发 action.deregister() | testRegisterWithDriverAction |
| getConnection(null) → SQLException("url cannot be null","08001")、无驱动 → "No suitable driver"、假驱动返回假连接 + user/password 属性 | testGetConnection |
| getDriver 命中 / 未命中 → SQLException | testGetDriver |

## 关键规范细节
- `getConnection(null)` 抛 SQLException，SQLState `08001`，message 含 "url cannot be null"。
- `deregisterDriver(null)` 是**无操作**（不抛）；`registerDriver(null)` 抛 NPE。
- 匿名 `Driver` 需实现 7 个方法（含 JDBC 4.1 的 `getParentLogger()`）。
- 用 `java.lang.reflect.Proxy` 实现 Connection，`connect()` 返回该代理即可验证 getConnection 通路，无需真实数据库。
- loginTimeout/logWriter 为全局静态，测试在 finally 中恢复原值。
