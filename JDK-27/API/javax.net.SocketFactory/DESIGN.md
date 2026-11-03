# javax.net.SocketFactory 黑盒行为测试设计

## 1. API 功能
抽象类，工厂/静态方法：
- `getDefault()`：返回环境默认 SocketFactory（JDK 实现返回 DefaultSocketFactory）。
- `createSocket()`（非抽象）：基类默认抛 SocketException("Unconnected sockets not implemented")；默认工厂重写为返回未连接 Socket。
- `createSocket(String,int)`、`createSocket(String,int,InetAddress,int)`、`createSocket(InetAddress,int)`、`createSocket(InetAddress,int,InetAddress,int)`：抽象，创建并连接 socket。

## 2. Java 用户如何使用
`SocketFactory.getDefault().createSocket("host", port)` 拿默认工厂建连接；默认工厂的 `createSocket()` 返回未连接 Socket 供 `connect(SocketAddress)` 使用。用户通常不直接实现，只通过 `getDefault()` 拿实例。

## 3. 开源代码实际用法调研
JDK 自身 `sun.net.www.protocol.https.HttpsClient` 用 `SSLSocketFactory.getDefault()`（其父类即 SocketFactory）创建 socket；Netty/OkHttp 不直接用此类而是包装 `SocketChannel`。权威用法见 `~/git/jdk25u-dev/test/jdk/javax/net/ssl/templates/SSLSocketTemplate.java` 中 `SSLSocketFactory.getDefault().createSocket(...)`。

## 4. 测试用例设计 + 自动化实现
黑盒：getDefault 幂等；createSocket() 返回未连接 socket；基类 createSocket() 抛 SocketException（自定义不重写 createSocket() 的子类）；createSocket(host,port)/createSocket(addr,port) 用 ephemeral loopback echo 服务器验证往返。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
