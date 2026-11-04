# ServerSocket 设计文档

## 一、API 功能

TCP 服务端监听套接字，绑定本地端口并 accept() 接受连接。

## 二、Java 用户如何使用

用户 new ServerSocket(0) 绑定临时端口，getLocalPort() 获取端口，accept() 得到连接的 Socket，close() 释放。

## 三、开源代码实际用法调研

Web 服务器、RMI 注册表、应用服务器都基于 ServerSocket 监听。

## 四、测试用例设计

new ServerSocket(0) 临时端口、isBound/getLocalPort/getLocalSocketAddress、setSoTimeout/getSoTimeout、accept 后读写、close 后 accept 抛异常。

## 批次 F 深化补充（167 断言）

- 状态机：unbound（isBound=false、accept/bind 前异常）→ bound（双 bind→BindException）→ closed（accept→"Socket is closed"）。
- 回环 accept：daemon acceptor + 队列同步；accept 返回的 socket 已连接已绑定；SoTimeout 边界（0=阻塞语义、负值→IAE）。
- accept 超时：SocketTimeoutException "Accept timed out"，超时后 server 仍可 accept。
- 选项：SO_RCVBUF 正/负值、SO_TIMEOUT、setSocketFactory 一次性（在 main 末尾执行）。
- `testServerSocketOptionApi`（JDK 9+）：getOption/setOption 往返、SO_REUSEADDR 查询。
- close 后 getLocalPort 保留原值；重复 close 幂等。
