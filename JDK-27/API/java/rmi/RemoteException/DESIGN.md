# java.rmi.RemoteException 行为测试设计

## 1. API 功能
`RemoteException` 是 RMI 通信相关异常的公共父类，继承 `java.io.IOException`。
- 构造器：`RemoteException()`、`RemoteException(String)`、`RemoteException(String, Throwable)`。
- 公共字段 `detail`（Throwable）：构造时传入的 cause；`getCause()` 返回 `detail`。
- `getMessage()`：无 cause 时返回父类消息；有 cause 时返回「消息 + "; nested exception is: \n\t" + detail.toString()」。
- `initCause()` 恒抛 `IllegalStateException`（构造器已调用 `initCause(null)` 禁止后续设置）。

## 2. Java 用户如何使用
- 自定义远程接口方法 `throws RemoteException`；实现里 `throw new RemoteException("op failed", e);`。
- 客户端 `catch (RemoteException e) { e.getCause(); e.getMessage(); }`。

## 3. 开源代码实际用法调研
- JDK 测试 `test/jdk/java/rmi/RemoteException/chaining/Chaining.java`（bug 4416970）：断言 `detail` 字段 == cause、`getCause()` == cause、`initCause` 抛 `IllegalStateException`，并验证「消息 = 前缀 + "; nested exception is: \n\t" + cause」。
- 类定义 `src/java.rmi/share/classes/java/rmi/RemoteException.java`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（RemoteExceptionTest.testBlackBox）覆盖：
- 无参构造：message/cause/detail 均为 null。
- String 构造：message 等于入参、cause/detail 为 null。
- (String, Throwable) 构造：cause==detail==同一对象、消息前缀 + 嵌套标记 + 含 cause.toString()。
- (String, null)：等价 String 构造。
- `initCause()` 抛 `IllegalStateException`。
白盒部分验证继承 IOException、非抽象/非 final、三个构造器形态、公共 `detail` 字段类型为 Throwable。
