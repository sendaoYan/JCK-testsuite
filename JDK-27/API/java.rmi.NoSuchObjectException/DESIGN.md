# java.rmi.NoSuchObjectException 行为测试设计

## 1. API 功能
`NoSuchObjectException` 继承 `java.rmi.RemoteException`，仅一个构造器 `NoSuchObjectException(String)`。

## 2. Java 用户如何使用
- 服务端在远程对象已注销（unexport）后仍被调用时抛出：`throw new NoSuchObjectException("object no longer exported");`。

## 3. 开源代码实际用法调研
- 类定义 `src/java.rmi/share/classes/java/rmi/NoSuchObjectException.java`。
- RemoteException 链式语义基准见 `test/jdk/java/rmi/RemoteException/chaining/Chaining.java`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（NoSuchObjectExceptionTest.testBlackBox）：String 构造 message、cause null、detail null。
白盒部分（testWhiteBox）：继承 RemoteException、非抽象/非 final、单个 String 构造器。
