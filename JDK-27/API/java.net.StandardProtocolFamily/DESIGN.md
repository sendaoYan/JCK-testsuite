# StandardProtocolFamily 设计文档

## 一、API 功能

标准协议族枚举：INET、INET6、UNIX。

## 二、Java 用户如何使用

用户用 StandardProtocolFamily.INET/UNIX 作为通道打开的协议族参数。

## 三、开源代码实际用法调研

NIO 通道编程按协议族创建 SocketChannel/ServerSocketChannel。

## 四、测试用例设计

枚举值 INET/INET6/UNIX 存在且顺序/name 正确；valueOf 往返；实现 ProtocolFamily。
