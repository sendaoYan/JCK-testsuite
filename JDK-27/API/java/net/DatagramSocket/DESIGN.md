# DatagramSocket 设计文档

## 一、API 功能

UDP 数据报套接字，支持无连接 send/receive 与有连接 connect/disconnect。

## 二、Java 用户如何使用

用户 new DatagramSocket(0) 绑定临时端口，配合 DatagramPacket send/receive；connect 后使用目标地址固定。

## 三、开源代码实际用法调研

DNS 客户端、日志采集、游戏服务器等 UDP 应用使用。

## 四、测试用例设计

绑定临时端口、send/receive 回环往返、connect/disconnect 与远端端点、SoTimeout 超时、广播/复用选项、close。
