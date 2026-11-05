# javax.rmi.ssl.SslRMIClientSocketFactory 行为测试设计

## 1. API 功能
- 类，`implements RMIClientSocketFactory, Serializable`；无参构造器。
- 方法：`Socket createSocket(String host, int port)`、`equals(Object)`、`hashCode()`。
- 使用默认 `SSLSocketFactory` 创建 SSL 客户端 socket。

## 2. Java 用户如何使用
- `new SslRMIClientSocketFactory()` 作为 RMI 客户端 socket 工厂传入导出/注册表 API。
- `equals` 仅比较类相等（任意两个默认实例相等），`hashCode` 为类 hashCode。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/javax/rmi/ssl/SslRMIClientSocketFactory.java`：`equals` 为 `obj.getClass().equals(this.getClass())`，`hashCode` 为 `this.getClass().hashCode()`。
- JDK 测试 `test/jdk/javax/rmi/ssl/SocketFactoryTest.java`：自定义子类 + `new SslRMIClientSocketFactory()`。
- JDK 测试 `test/jdk/javax/rmi/ssl/SSLSocketParametersTest.java`：客户端/服务端工厂配对。

## 4. 测试用例设计 + 自动化实现
- 白盒：public、implements RMIClientSocketFactory/Serializable、`createSocket(String,int)` 签名。
- 黑盒：两默认实例 equals/hashCode 相等；不等于 null/其他类型；Java 序列化往返保持 equals/hashCode。
- 实现见 `SslRMIClientSocketFactoryTest.java`。

## 5. 深化补充（本轮契约深化）
新增：无状态值语义（任意两实例 equals 且 hashCode 相等）、序列化往返保持 equals/hashCode、
`createSocket(null,0)` 与已关端口 → IOException 精确抛出、白盒两接口形态。
