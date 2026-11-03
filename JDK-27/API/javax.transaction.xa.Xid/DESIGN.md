# javax.transaction.xa.Xid 行为测试设计

## 1. API 功能
- 接口，3 个抽象方法：`getFormatId()`、`getGlobalTransactionId()`、`getBranchQualifier()`。
- 常量：`MAXGTRIDSIZE = 64`（全局事务 ID 最大长度）、`MAXBQUALSIZE = 64`（分支限定符最大长度）。

## 2. Java 用户如何使用
- 事务管理器/资源管理器实现 `Xid` 标识一个全局事务分支；`getFormatId` 区分格式，`getGlobalTransactionId` 唯一标识全局事务，`getBranchQualifier` 标识分支。
- 实现者应保证两个字节数组长度不超过 64。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.transaction.xa/share/classes/javax/transaction/xa/Xid.java`：接口与常量。
- JTA 规范定义 Xid 结构；数据库驱动（如 PostgreSQL/MySQL XA）实现该接口。
- JDK 测试 `test/jdk/javax/transaction/xa/` 下驱动类引用 Xid 方法。

## 4. 测试用例设计 + 自动化实现
- 白盒：interface、public、无父接口、3 方法、`MAXGTRIDSIZE`/`MAXBQUALSIZE` 均为 64。
- 黑盒：匿名实现 3 方法；`getFormatId`/`getGlobalTransactionId`/`getBranchQualifier` 哨兵返回；构造长度恰为 64 的合法 Xid。
- 实现见 `XidTest.java`。
