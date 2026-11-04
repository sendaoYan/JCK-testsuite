# java.sql.SQLTimeoutException 测试设计案（JDK-8）

> 完整契约：层次（SQLTransientException → SQLException）+ 8 构造器默认值。

## 1. API 提供功能
- 构造器（8）：同 SQLFeatureNotSupportedException 的 8 个重载。

## 2. 使用方式
连接/语句超时时抛出（transient 类：可重试）。

## 3. 参考来源
- javadoc；源码 `~/git/jdk8u-dev/.../java/sql/SQLTimeoutException.java`；JCK `api/java_sql/SQLTimeoutException`。

## 4. 测试用例设计
| 测试点 | 用例方法 |
|---|---|
| 层次 | testHierarchy |
| 8 构造器 + 默认值 | testConstructors |
