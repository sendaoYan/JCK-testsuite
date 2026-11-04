# javax.net.ServerSocketFactory 黑盒行为测试设计

## 1. API 功能
抽象类：
- `getDefault()`：返回默认 ServerSocketFactory。
- `createServerSocket()`（非抽象）：基类默认抛 SocketException；默认工厂重写为返回未绑定 ServerSocket。
- `createServerSocket(int)` / `(int,int)` / `(int,int,InetAddress)`：抽象，创建绑定监听 socket。

## 2. Java 用户如何使用
`ServerSocketFactory.getDefault().createServerSocket(0, backlog, loopback)` 绑定 ephemeral 端口；`createServerSocket()` 拿未绑定 socket 再 `bind()`。

## 3. 开源代码实际用法调研
`~/git/jdk25u-dev/test/jdk/javax/net/ssl/templates/` 的 SSLEngine 模板用 `SSLServerSocketFactory.getDefault().createServerSocket(0, backlog, addr)` 绑定临时端口；Apache Commons/Spring 内嵌服务器使用工厂抽象创建监听 socket。

## 4. 测试用例设计 + 自动化实现
黑盒：getDefault 非空；createServerSocket() 返回未绑定 socket；createServerSocket(0,10,loopback) 绑定 ephemeral + loopback；用 try-with-resources 关闭。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
