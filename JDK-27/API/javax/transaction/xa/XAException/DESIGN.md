# javax.transaction.xa.XAException 行为测试设计

## 1. API 功能
- 类，`extends Exception`；公开可变字段 `int errorCode`。
- 构造器：`XAException()`、`XAException(String s)`、`XAException(int errcode)`。
- 常量：回滚码 `XA_RBBASE/XA_RBROLLBACK/XA_RBCOMMFAIL/XA_RBDEADLOCK/XA_RBINTEGRITY/XA_RBOTHER/XA_RBPROTO/XA_RBTIMEOUT/XA_RBTRANSIENT/XA_RBEND`；启发式码 `XA_NOMIGRATE/XA_HEURHAZ/XA_HEURCOM/XA_HEURRB/XA_HEURMIX`；错误码 `XAER_ASYNC/XAER_RMERR/XAER_NOTA/XAER_INVAL/XAER_PROTO/XAER_RMFAIL/XAER_DUPID/XAER_OUTSIDE`；`XA_RETRY/XA_RDONLY`。

## 2. Java 用户如何使用
- `new XAException(XAException.XAER_RMERR)` 设置错误码；`ex.errorCode` 读取/修改。
- `new XAException("message")` 设置消息（errorCode 为 0）。
- 注意：`XAException(int)` 不设置 message（`getMessage()` 为 null）。

## 3. 开源代码实际用法调研
- JDK 测试 `test/jdk/javax/transaction/xa/testng/test/transaction/XAExceptionTests.java`：逐一断言各构造器 errorCode 与 message。
- JDK 测试 `test/jdk/javax/transaction/xa/testng/util/SerializedTransactionExceptions.java`：XAException 序列化。
- JDK 源码 `src/java.transaction.xa/share/classes/javax/transaction/xa/XAException.java`：常量定义与构造器。

## 4. 测试用例设计 + 自动化实现
- 白盒：public、父类 Exception。
- 黑盒：无参 errorCode 0/message null；`int` 构造 errorCode 7/message null；`String` 构造 message/errorCode 0；errorCode 可变；全部常量精确值。
- 实现见 `XAExceptionTest.java`。
