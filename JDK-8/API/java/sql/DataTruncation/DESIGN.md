# java.sql.DataTruncation 测试设计案（JDK-8）

> 完整契约：读/写 SQLState（01004/22001）、message/vendor code、5 个字段（含 -1 哨兵）、cause 构造器、序列化。

## 1. API 提供功能
- 构造器（2）：`(int index,boolean parameter,boolean read,int dataSize,int transferSize)`、同 + `Throwable cause`。
- 访问器：`getIndex/getParameter/getRead/getDataSize/getTransferSize` + 继承的 `getSQLState/getMessage/getErrorCode/getCause`。

## 2. 使用方式
读/写被截断时由驱动抛出/报告；`read=true` 读截断（SQLState 01004），`read=false` 写截断（SQLState 22001）。

## 3. 参考来源
- javadoc `java/sql/DataTruncation.html`；源码 `~/git/jdk8u-dev/.../java/sql/DataTruncation.java`；JCK `api/java_sql/DataTruncation`。

## 4. 测试用例设计
| 测试点 | 用例方法 |
|---|---|
| 读截断：SQLState 01004、message、字段 | testReadTruncation |
| 写截断：SQLState 22001、-1 哨兵 | testWriteTruncation |
| cause 构造器 | testCauseConstructor |
| 层次（extends SQLWarning） | testHierarchy |
| 序列化 round-trip + serialVersionUID=6464298989504059473L | testSerialization |

## 关键规范细节
- SQLState 由 `read` 决定：true→`01004`，false→`22001`；message 恒 `"Data truncation"`；vendor code 恒 0。
- `index/dataSize/transferSize` 可为 -1 表示未知。
