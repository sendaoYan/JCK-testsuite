# javax.sql.DataSource 测试设计案（JDK-8）

> 完整契约：接口层次（CommonDataSource + Wrapper）、精确方法集（getConnection × 2 + 继承方法）、
> 动态代理实现验证每个方法可调用与异常（不真连库）。

## 1. API 提供功能
- 接口 `javax.sql.DataSource extends CommonDataSource, java.sql.Wrapper`。
- 自身方法（2）：`getConnection()`、`getConnection(String,String)`（均 throws SQLException）。
- 继承 CommonDataSource：`getLogWriter/setLogWriter/setLoginTimeout/getLoginTimeout/getParentLogger`。
- 继承 Wrapper：`unwrap/isWrapperFor`。

## 2. 使用方式
`ds.getConnection()` 拿连接；`getLogWriter/getLoginTimeout` 配置；`unwrap(Connection.class)` 解包。

## 3. 参考来源
- javadoc `javax/sql/DataSource.html`、`javax/sql/CommonDataSource.html`；JDK-27 额外有 `createConnectionBuilder()` 默认方法 + CommonDataSource `createShardingKeyBuilder()`。

## 4. 测试用例设计
| 测试点 | 用例方法 |
|---|---|
| 接口层次（CommonDataSource + Wrapper） | testHierarchy |
| 精确方法集（2 个 getConnection 重载 + 继承方法） | testMethodSet |
| 动态代理实现：getConnection 返回假连接、getParentLogger 抛 SQLFeatureNotSupportedException、unwrap 抛 SQLException | testProxyInvocation |

## 关键规范细节
- `getParentLogger()` 声明 throws `SQLFeatureNotSupportedException`；`unwrap(非包装类型)` 抛 SQLException。
- JDK-27 侧额外断言 `createConnectionBuilder()`/`createShardingKeyBuilder()` 为 default 且返回类型正确。
