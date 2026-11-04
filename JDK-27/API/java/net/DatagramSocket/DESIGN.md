# DatagramSocket 设计文档

## 一、API 功能

UDP 数据报套接字，支持无连接 send/receive 与有连接 connect/disconnect。

## 二、Java 用户如何使用

用户 new DatagramSocket(0) 绑定临时端口，配合 DatagramPacket send/receive；connect 后使用目标地址固定。

## 三、开源代码实际用法调研

DNS 客户端、日志采集、游戏服务器等 UDP 应用使用。

## 四、测试用例设计

绑定临时端口、send/receive 回环往返、connect/disconnect 与远端端点、SoTimeout 超时、广播/复用选项、close。

---

# 深化批次 G 追加（2026-11-05，~50 → 200 断言级契约覆盖）

## 覆盖缺口分析（深化前只有 ~50 断言冒烟）

缺：未绑定状态矩阵、隐式绑定、bind 已绑/占用端口负例、构造器端口校验精确消息、
connect 过滤实测、无地址包 send 语义、receive 超时精确消息、关闭状态机全集、
SocketOption API（JDK-27 新增）、一次性 factory 契约。

## 新增测试方法与断言点

| 方法 | 覆盖 |
|---|---|
| testWhiteBox27 | JDK-27 实例方法全部非 synchronized（JDK-8 是 synchronized）、setOption/getOption/supportedOptions/joinGroup(SA,NI) 存在、4 构造器签名 |
| testSocketOptionApi | supportedOptions 含 SO_BROADCAST/SO_RCVBUF/SO_SNDBUF/SO_REUSEADDR/IP_TOS；setOption/getOption 往返；关闭后 getOption → ClosedChannelException（NIO 实现） |
| testUnboundAndImplicitBind | new DatagramSocket(null)：isBound=false/port=0/wildcard/lsa=null/getPort=-1；send/receive 隐式绑定 |
| testBindMatrixDeep | bind(null)→wildcard、双重 bind→SocketException、占用端口→BindException、closed bind |
| testPortValidationDeep | 构造器端口 -1/-2/65536/65537/MAX/MIN → IAE 精确 "port out of range:<v>"（JDK-27 措辞）；65535 合法 |
| testConnectMatrixDeep | connect 状态/远端端点；端口校验同措辞；connect(lo,0) → UncheckedIOException（JDK-27 特有）；connect 1.2.3.4 不发包；unresolved → SocketException "Unresolved address"；错误 SA 类型 → UnsupportedAddressTypeException |
| testConnectFilteringDeep | 三 socket 回环：connect 方发包被过滤（SocketTimeoutException）、已连接方无地址 send 送达目标 |
| testSendReceiveSemanticsDeep | offset 窗口往返精确字节、坏端口包 IAE "Port out of range:2147483647"、无地址包→IAE（JDK-8 是 NPE）、send/receive(null)→NPE、自发自收有序 |
| testOptionsMatrixDeep | SoTimeout 默认 0/往返/负 IAE "timeout < 0"；buffer 默认>0/64k 精确；trafficClass 0x28 往返；broadcast 默认 true；reuseAddress 未绑定往返 |
| testCloseStateMachineDeep | close 后 send/receive/bind/get/setSoTimeout→SocketException；getLocalPort=-1/getLocalAddress=null；isBound 保持 true；close 幂等；connect on closed 为 no-op |
| testMiscExtras27 | 零长数据报、wildcard lsa 形态、SO_REUSEADDR 经 SocketOption API 往返 |
| testImplFactoryOneShot27 | 二次 setDatagramSocketImplFactory → SocketException "factory already defined"（精确）；null impl → new DatagramSocket NPE |

## 版本差异（探针实测，双侧一致才断精确文本）

1. JDK-27 实例方法非 synchronized；JDK-8 bind/receive/选项族 synchronized（static factory setter 两侧均 synchronized）。
2. connect(InetAddress,int) 端口非法消息：JDK-27 "port out of range:65536"，JDK-8 "connect: 65536"。
3. connect(lo, 0)：JDK-27 → UncheckedIOException("Can't connect to port 0")；JDK-8 记录状态成功。
4. connect(错误 SA 类型)：JDK-27 → UnsupportedAddressTypeException；JDK-8 → IAE。
5. 未连接 socket send 无地址包：JDK-27 → IAE("Address not set")；JDK-8 → NPE。
6. 关闭后操作消息大小写差异（"Socket closed"/"Socket is closed"/"Already bound"）→ 只断类型。
7. JDK-27 独有 setOption/getOption/supportedOptions（11 个选项）与 joinGroup/leaveGroup(SA,NI)。
8. JDK-27 关闭后 getOption → ClosedChannelException（非 SocketException 族）。
