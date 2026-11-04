# MulticastSocket 设计文档

## 一、API 功能

UDP 组播套接字，继承 DatagramSocket，支持 TTL、加入/离开组、组播接口与回环模式。

## 二、Java 用户如何使用

用户 new MulticastSocket(0) 后 joinGroup(group) 加入组，setTimeToLive 设 TTL，send/receive 收发组播报文。

## 三、开源代码实际用法调研

组播视频/音频、服务发现(mDNS)等使用。

## 四、测试用例设计

setTimeToLive/getTimeToLive、set/getLoopbackMode、set/getInterface 回环、set/getNetworkInterface；继承的 DatagramSocket 单播回环往返；close。
