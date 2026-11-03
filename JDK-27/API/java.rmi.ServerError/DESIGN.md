# java.rmi.ServerError 行为测试设计

## 1. API 功能
`ServerError` 继承 `java.rmi.RemoteException`，表示服务端抛出 `Error`。唯一构造器 `ServerError(String, Error)`。

## 2. Java 用户如何使用
- 服务端 RMI 分发过程中捕获 `Error` 时包装：`throw new ServerError("server error", error);`。

## 3. 开源代码实际用法调研
- 类定义 `src/java.rmi/share/classes/java/rmi/ServerError.java`。
- 链式语义基准 `test/jdk/java/rmi/RemoteException/chaining/Chaining.java`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（ServerErrorTest.testBlackBox）：cause==detail==Error、cause instanceof Error、消息前缀 + 嵌套标记；(String,null) 等价单 String。
白盒部分（testWhiteBox）：继承 RemoteException、非抽象/非 final、构造器 (String, Error)。
