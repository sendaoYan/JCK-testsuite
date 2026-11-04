# java.sql.SQLWarning 测试设计案（JDK-8）

> 完整契约：继承 SQLException 的 8 构造器 + setNextWarning 链 + getNextWarning 对非 warning 链抛 Error + 序列化。

## 1. API 提供功能
- 构造器（8）：同 SQLException。
- `getNextWarning()/setNextWarning(SQLWarning)`：别名 `getNextException/setNextException`。
- 继承 `Iterable<Throwable>` 与 `getSQLState/getErrorCode/getCause`。

## 2. 使用方式
从 Connection/Statement/ResultSet 取 warning → `getNextWarning` 遍历。

## 3. 参考来源
- javadoc `java/sql/SQLWarning.html`；源码 `~/git/jdk8u-dev/.../java/sql/SQLWarning.java`；JCK `api/java_sql/SQLWarning`。

## 4. 测试用例设计
| 测试点 | 用例方法 |
|---|---|
| 8 构造器 + 默认值 | testConstructors |
| setNextWarning 追加到链尾 + getNextException 别名 | testChain |
| getNextWarning 对链中非 SQLWarning → Error | testGetNextWarningErrorOnNonWarning |
| iterator 遍历 warning 链 | testIterator |
| 层次（extends SQLException） | testHierarchy |
| 序列化 round-trip + serialVersionUID=3917336774604784856L | testSerialization |

## 关键规范细节
- `getNextWarning()` 强转 `(SQLWarning)getNextException()`；若链里混入普通 SQLException 会抛 `Error`（不是 SQLException）。
