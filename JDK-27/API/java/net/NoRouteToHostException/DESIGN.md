# NoRouteToHostException 设计文档

## 一、API 功能

无法路由到目标主机(ICMP 不可达)时抛出的 SocketException 子类。

## 二、Java 用户如何使用

用户在 Socket.connect 时捕获(通常表现为 ConnectException 的替代)。

## 三、开源代码实际用法调研

网络诊断工具使用。

## 四、测试用例设计

继承 SocketException；直接构造与带 message 构造。
