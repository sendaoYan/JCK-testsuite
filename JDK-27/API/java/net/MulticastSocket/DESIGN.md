# MulticastSocket 设计文档

## 一、API 功能

UDP 组播套接字，继承 DatagramSocket，支持 TTL、加入/离开组、组播接口与回环模式。

## 二、Java 用户如何使用

用户 new MulticastSocket(0) 后 joinGroup(group) 加入组，setTimeToLive 设 TTL，send/receive 收发组播报文。

## 三、开源代码实际用法调研

组播视频/音频、服务发现(mDNS)等使用。

## 四、测试用例设计

setTimeToLive/getTimeToLive、set/getLoopbackMode、set/getInterface 回环、set/getNetworkInterface；继承的 DatagramSocket 单播回环往返；close。

---

# 深化批次 G 追加（2026-11-05，~30 → 101 断言级契约覆盖）

## 覆盖缺口分析

缺：legacy setTTL(byte)/getTTL()/send(pkt,ttl) 在 JDK-27 已移除的负向验证、
构造器矩阵（延迟绑定）、TTL 边界（0/255 合法、-1/256 IAE）、接口/网络接口默认值与
非法值、joinGroup/leaveGroup 校验矩阵（null NPE、非组播地址精确消息、错误 SA 类型 IAE）、
真实组播数据通路（join→send→receive 精确字节、leave 后超时、双成员收包）、
关闭后 SocketException 全集、SocketOption API 上 IP_MULTICAST_TTL 往返。

## 新增测试方法

| 方法 | 覆盖 |
|---|---|
| testLegacyTtlFamilyRemoved | 反射验证 setTTL(byte)/getTTL()/send(DatagramPacket,byte) 在 JDK-27 不存在（JDK-8 仍存在且另有行为断言） |
| testConstructorsDeep | ctor(0)/ctor(SA loopback)/ctor(null) 延迟绑定 + bind 后 loopback 形态 |
| testTtlMatrixDeep | 默认 1；0/1/128/255 往返；-1/256/MAX → IAE（JDK-27 消息 "Invalid TTL/hop value" vs JDK-8 "ttl out of range"，仅断类型） |
| testInterfaceMatrixDeep | 默认 getInterface()==wildcard；setInterface(loopback) 回读 loopback；setInterface(0.0.0.0) → SocketException |
| testNetworkInterfaceMatrixDeep | 默认 getNetworkInterface() 非 null；setNetworkInterface(lo) equals 回读；setNetworkInterface(null) → **IAE**（JDK-8 是 SocketException） |
| testJoinLeaveValidationDeep | joinGroup(null) NPE；joinGroup(127.0.0.1) → SocketException "Not a multicast address"（精确，双侧同）；joinGroup(null SA, NI) → IAE "Unsupported address type"；joinGroup(SA,null NI) 默认接口可用；未 join 先 leave → SocketException；239.x join/leave 往返 |
| testGroupDataPathDeep | 239.254.4.11 + TTL 0 + wildcard 绑定接收方：join→send→receive 精确 4 字节（DE AD BE EF）、源端口/地址填充；leave 后同发 → SocketTimeoutException（无投递） |
| testTwoReceiverGroup | 同组两成员各自收到数据报 |
| testInheritedExtras | 继承 SoTimeout/connect/disconnect/getChannel/toString；零长单播数据报 |
| testMiscExtras27 | 224.0.0.1 join/leave；localSocketAddress 形态；supportedOptions 含 IP_MULTICAST_TTL/IP_MULTICAST_LOOP；setOption(IP_MULTICAST_TTL,9) 与 getTimeToLive() 互见 |
| testClosedSemanticsDeep | close 后 joinGroup/set-getTimeToLive/get-setInterface/getNetworkInterface/getLoopbackMode → SocketException；close 幂等 |

## 环境说明与版本差异

- 本环境（容器/物理混合）：lo 不支持组播（supportsMulticast()==false），默认组播接口为
  10.59.42.204 所在网卡；join 239.x 成功、TTL 0 组播本机可收（wildcard 绑定接收方）——
  以上均经双侧探针多次复跑确认确定性；接收包源地址断言仅"非 null"（实际是 10.59.42.204，非回环）。
- JDK-8 特有断言（在其侧文件）：setTTL(9)→getTimeToLive 9；setTTL(-1)→TTL 255/getTTL -1；
  setTTL(200)→getTTL -56（字节截断）；废弃 send(pkt, ttl) 单播往返且 socket TTL 不变。
- setTimeToLive(-1/256) 消息、setNetworkInterface(null) 异常类型、leaveGroup-未加入消息均双侧不同 → 各断类型并记录。
