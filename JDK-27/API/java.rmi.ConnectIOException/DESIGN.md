# java.rmi.ConnectIOException 行为测试设计

## 1. API 功能
ConnectIOException 是 RMI 远程调用过程中的一个受检异常，继承自 `java.rmi.RemoteException`（因此也是 `java.io.IOException`）。
- 构造器：`ConnectIOException(String)` 与 `ConnectIOException(String, Exception)`。
- 继承自 `RemoteException` 的行为：`getMessage()` 在存在 cause 时返回「外层消息 + "; nested exception is: \n\t" + cause.toString()」；`getCause()` 返回构造时传入的 cause；公共字段 `detail` 与 cause 保持一致。


## 2. Java 用户如何使用
- `catch (RemoteException e)` 捕获后调用 `e.getMessage()` / `e.getCause()` 记录日志。
- 构造自定义异常时：`throw new ConnectIOException("connect failed", new java.net.ConnectException("refused"));`。
- 序列化到客户端时，服务端抛出的异常以 `detail` 字段携带原始 cause。

## 3. 开源代码实际用法调研
- JDK 自身 `test/jdk/java/rmi/RemoteException/chaining/Chaining.java`（bug 4416970）验证 `detail` 字段、`getCause()` 与 `initCause()` 抛 `IllegalStateException` 的链式语义，是所有 RemoteException 子类的行为基准。
- 类定义见 `src/java.rmi/share/classes/java/rmi/ConnectIOException.java`（`~/git/jdk25u-dev`）。

## 4. 测试用例设计 + 自动化实现
黑盒部分（ConnectIOExceptionTest.testBlackBox）覆盖：
- 单 String 构造：`getMessage()` 等于入参、`getCause()==null`、`detail==null`。
- (String, Exception) 构造：`getCause()` 与 `detail` 为同一对象、`getMessage()` 前缀与嵌套标记。
- (String, null) 构造：等价于单 String 构造。
- 两个同消息异常不相等（异常按身份比较）。
白盒部分（testWhiteBox）验证继承自 RemoteException、非抽象/非 final、构造器参数形态。
