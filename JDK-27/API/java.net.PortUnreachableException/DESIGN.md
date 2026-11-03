# PortUnreachableException 设计文档

## 一、API 功能

UDP 报文发送后收到 ICMP 端口不可达时抛出的 SocketException 子类。

## 二、Java 用户如何使用

用户在 DatagramSocket.send/receive 时捕获。

## 三、开源代码实际用法调研

UDP 应用检测对端关闭端口。

## 四、测试用例设计

继承 SocketException；构造器。
