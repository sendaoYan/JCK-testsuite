# MulticastSocket 设计文档（JDK-8）

## 一、API 功能

UDP 组播套接字（extends DatagramSocket）：TTL（int 新 API + byte 旧 API）、
joinGroup/leaveGroup（InetAddress 与 SocketAddress+NetworkInterface 两种）、
组播接口（set/getInterface、set/getNetworkInterface）、回环模式（set/getLoopbackMode）、
废弃的 send(DatagramPacket, byte)。

## 二、Java 用户如何使用

`new MulticastSocket(0)` 后 `joinGroup(group)`、`setTimeToLive`，send/receive 收发组播；
单播收发直接沿用 DatagramSocket 语义。

## 三、参考来源

- Oracle Java SE 8 javadoc（java/net/MulticastSocket.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/MulticastSocket/`。
- 双 JDK 探针实测（/tmp/probe-g，2026-11-05）。

## 四、测试用例设计（MulticastSocketTest.java，104 断言）

| 方法 | 覆盖 |
|---|---|
| testLegacySmoke | 保留原冒烟断言（bind/close） |
| testWhiteBox | extends DatagramSocket、3 构造器、setTTL/getTTL/send(pkt,ttl) 存在、全方法表 |
| testConstructors | ()/(0)/(SA loopback)/(null 延迟绑定+bind) 四入口 |
| testTtlMatrix | 默认 1；int API 0/1/128/255 往返 + -1/256/MAX IAE；**byte 旧 API**：setTTL(9)↔getTimeToLive 9、setTTL(-1)→TTL 255/getTTL -1、setTTL(200)→getTTL -56（字节截断）、setTimeToLive 经 getTTL 可见 |
| testInterfaceMatrix | 默认 getInterface()==wildcard；setInterface(loopback) 回读；setInterface(0.0.0.0) → SocketException |
| testNetworkInterfaceMatrix | 默认非 null；setNetworkInterface(lo) equals 回读；setNetworkInterface(null) → SocketException（JDK-27 为 IAE） |
| testLoopbackMode | false/true 往返 |
| testJoinLeaveValidation | joinGroup(null) NPE；joinGroup(127.0.0.1) → SocketException "Not a multicast address"（精确）；joinGroup(null SA, NI) IAE "Unsupported address type"；joinGroup(SA,null NI) 可用；未 join 先 leave → SocketException；239.x 双口味 join/leave |
| testGroupDataPath | 239.254.4.11 + TTL 0 + wildcard 接收方：join→send→receive 精确 DE AD BE EF、源地址/端口填充；leave 后 → SocketTimeoutException |
| testTwoReceiverGroup | 同组两成员各自收包 |
| testDeprecatedSendWithTtl | 废弃 send(pkt,(byte)1) 单播回环精确字节；socket 自身 getTimeToLive 保持 3 不变 |
| testInheritedDatagramBehavior | 单播回环、connect/disconnect、getChannel null、SoTimeout 往返、toString |
| testClosedSemantics | close 后 8 个操作 → SocketException；close 幂等 |

## 五、易错规范细节与版本差异

- TTL=0 组播数据报仅本机投递；接收方必须 wildcard 绑定（本环境 lo 不支持组播，
  组成员籍落在默认网卡 10.59.42.204）——源地址断言只做"非 null"。
- 旧 API setTTL(byte) 负值按无符号字节解释（-1 ↔ 255、200 ↔ -56）。
- 废弃 send(pkt, ttl) 只影响该包，socket TTL 不变。
- setTimeToLive(-1/256) 消息 JDK-8 "ttl out of range" vs JDK-27 "Invalid TTL/hop value" → 只断类型。
- setNetworkInterface(null)：JDK-8 SocketException / JDK-27 IAE。
- JDK-13+ 移除 setTTL/getTTL/send(pkt,ttl)（JDK-27 侧以反射验证其不存在）。
