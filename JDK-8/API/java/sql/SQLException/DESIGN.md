# java.sql.SQLException 测试设计案（JDK-8）

> 完整契约：8 个构造器矩阵、默认值、setNextException 追加语义、Iterable<Throwable> 迭代顺序、序列化。

## 1. API 提供功能
- 构造器（8）：`()`、`(String)`、`(String,String)`、`(String,String,int)`、`(Throwable)`、`(String,Throwable)`、`(String,String,Throwable)`、`(String,String,int,Throwable)`。
- 访问器：`getMessage/getSQLState/getErrorCode/getNextException/getCause`。
- 链：`setNextException(SQLException)`（追加到链尾）。
- `iterator()`：`Iterable<Throwable>`，顺序 = 自身 → 自身 cause 链 → 下一异常 → 下一异常 cause 链 → …；`remove()` → UOE；耗尽 `next()` → NSEE。

## 2. 使用方式
抛/接住异常 → 读 SQLState/errorCode → `getNextException` 或 `for(Throwable t : ex)` 遍历整链。

## 3. 参考来源
- javadoc `java/sql/SQLException.html`；源码 `~/git/jdk8u-dev/.../java/sql/SQLException.java`（iterator 顺序、CAS 追加）；JCK `api/java_sql/SQLException`。

## 4. 测试用例设计
| 测试点 | 用例方法 |
|---|---|
| 8 构造器 + message/SQLState/errorCode 默认值 | testConstructors |
| cause/next 默认 null | testGettersDefaults |
| 4 个 cause 构造器（message=cause.toString() 等） | testCauseConstructors |
| setNextException 追加到链尾 | testSetNextExceptionChain |
| iterator 顺序 + remove() UOE + 空链 next() NSEE | testIterator |
| 层次（extends Exception, implements Iterable） | testHierarchy |
| 序列化 round-trip（含 next 链）+ serialVersionUID=2135244094396331484L | testSerialization |

## 关键规范细节
- 迭代顺序不是「自身→下一异常」，而是**自身→自身 cause 链→下一异常→下一异常 cause 链→…**。
- `setNextException` 追加到**链尾**（遍历到 next==null 再 CAS 挂接）。
- `(Throwable)` / `(String,Throwable)` 构造器把 message 设为 `cause.toString()`（cause 非 null 时）。
