# javax.transaction.xa.XAResource 行为测试设计

## 1. API 功能
- 接口，10 个抽象方法：`commit(Xid, boolean)`、`end(Xid, int)`、`forget(Xid)`、`getTransactionTimeout()`、`isSameRM(XAResource)`、`prepare(Xid)`、`recover(int)`、`rollback(Xid)`、`setTransactionTimeout(int)`、`start(Xid, int)`。
- 常量：`TMENDRSCAN/TMFAIL/TMJOIN/TMNOFLAGS/TMONEPHASE/TMRESUME/TMSTARTRSCAN/TMSUCCESS/TMSUSPEND`、`XA_RDONLY`、`XA_OK`。

## 2. Java 用户如何使用
- 资源管理器实现该接口参与 XA 分布式事务；`start`/`end` 界定事务边界，`prepare`/`commit`/`rollback` 两阶段提交。
- `recover(TMSTARTRSCAN|TMENDRSCAN)` 恢复准备状态事务。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.transaction.xa/share/classes/javax/transaction/xa/XAResource.java`：接口与常量。
- JDK 测试 `test/jdk/javax/transaction/xa/testng/JavaSqlModuleDriver.java`、`Driver.java`：加载 XA 模块。
- 该接口是 JTA 规范核心，数据库/消息中间件驱动实现。

## 4. 测试用例设计 + 自动化实现
- 白盒：interface、public、无父接口、10 方法、11 常量精确值。
- 黑盒：匿名实现 10 方法；`getTransactionTimeout`/`setTransactionTimeout` 语义；`isSameRM` 自比较；`prepare` 返回 XA_OK 且回调触发；`recover` 返回 Xid 数组；`commit`/`start`/`end`/`rollback`/`forget` 可调用。
- 实现见 `XAResourceTest.java`。
