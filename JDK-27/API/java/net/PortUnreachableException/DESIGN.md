# PortUnreachableException 设计文档

## 一、API 功能

UDP 报文发送后收到 ICMP 端口不可达时抛出的 SocketException 子类。

## 二、Java 用户如何使用

用户在 DatagramSocket.send/receive 时捕获。

## 三、开源代码实际用法调研

UDP 应用检测对端关闭端口。

## 四、测试用例设计

继承 SocketException；构造器。

## 批次 F 深化补充（54 断言）

- serialVersionUID=8462541992376507323L（双 JDK 一致）。
- 真实触发（回环 UDP）：DatagramSocket 绑 127.0.0.1 临时端口后 close；另一 socket connect 该地址后 send+receive→PortUnreachableException（Linux 回环投递 ICMP）；JDK-27 消息为 null（JDK-8 为 "ICMP Port Unreachable"，按侧断言）。
- PUE 可作为 PUE 的 cause；cause/suppressed/序列化/跨线程全套。
