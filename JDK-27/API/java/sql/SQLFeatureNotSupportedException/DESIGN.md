# java.sql.SQLFeatureNotSupportedException 测试设计案（JDK-8）

> 完整契约：层次（SQLNonTransientException → SQLException）+ 8 构造器默认值。

## 1. API 提供功能
- 构造器（8）：`()`、`(String)`、`(String,String)`、`(String,String,int)`、`(Throwable)`、`(String,Throwable)`、`(String,String,Throwable)`、`(String,String,int,Throwable)`。

## 2. 使用方式
驱动对不支持的特性抛此异常（区别于「临时」的 SQLFeatureNotSupportedException 之 transient 兄弟）。

## 3. 参考来源
- javadoc；源码 `~/git/jdk8u-dev/.../java/sql/SQLFeatureNotSupportedException.java`；JCK `api/java_sql/SQLFeatureNotSupportedException`。

## 4. 测试用例设计
| 测试点 | 用例方法 |
|---|---|
| 层次 | testHierarchy |
| 8 构造器 + 默认值 | testConstructors |
