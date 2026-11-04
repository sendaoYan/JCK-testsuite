# java.rmi.ServerRuntimeException 行为测试设计

## 1. API 功能
`ServerRuntimeException` 继承 `java.rmi.RemoteException`，表示服务端抛出运行时异常。唯一构造器 `ServerRuntimeException(String, Exception)`。

## 2. Java 用户如何使用
- 服务端 RMI 分发中捕获 `RuntimeException` 时包装：`throw new ServerRuntimeException("runtime failure", re);`。

## 3. 开源代码实际用法调研
- 类定义 `src/java.rmi/share/classes/java/rmi/ServerRuntimeException.java`。
- 链式语义基准 `test/jdk/java/rmi/RemoteException/chaining/Chaining.java`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（ServerRuntimeExceptionTest.testBlackBox）：cause==detail、消息前缀 + 嵌套标记；(String,null) 等价单 String。
白盒部分（testWhiteBox）：继承 RemoteException、非抽象/非 final、构造器 (String, Exception)。
