# InetSocketAddress 设计文档

## 一、API 功能

IP 地址与端口号组成的套接字地址，支持未解析(hostname)地址与通配地址。

## 二、Java 用户如何使用

用户 new InetSocketAddress(host, port) 或 new InetSocketAddress(InetAddress, port) 作为 Socket.connect/bind 与 ServerSocket.bind 的参数。

## 三、开源代码实际用法调研

Netty、gRPC、OkHttp 等所有基于 TCP/UDP 的框架都用 InetSocketAddress 表示端点。

## 四、测试用例设计

构造解析/未解析地址并断言 getAddress/getHostName/getPort/getHostString/isUnresolved；通配地址自洽；equals 语义。
