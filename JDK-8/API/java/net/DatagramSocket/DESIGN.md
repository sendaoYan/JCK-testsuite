# DatagramSocket 设计文档（JDK-8）

## 一、API 功能

UDP 数据报套接字：无连接 send/receive、可选 connect（本机过滤语义）、bind/close 状态机、
8 个套接字选项（SoTimeout/SNDBUF/RCVBUF/ReuseAddress/Broadcast/TrafficClass）与一次性
DatagramSocketImplFactory 安装点。

## 二、Java 用户如何使用

`new DatagramSocket(0)` 绑定临时端口后配合 DatagramPacket 收发；`connect(127.0.0.1, port)`
后 receive 只收对端包、send 可省目标地址；`setSoTimeout` 给 receive 加界。

## 三、参考来源

- Oracle Java SE 8 javadoc（java/net/DatagramSocket.html：状态机与异常契约）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/DatagramSocket/`（许多 bind/connect/close 负例回归）。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_net/DatagramSocket/`。
- 双 JDK 探针实测（/tmp/probe-g，2026-11-05）：消息/默认值双侧对照后入库。

## 四、测试用例设计（DatagramSocketTest.java，200 断言）

| 方法 | 覆盖 |
|---|---|
| testLegacySmoke | 保留原冒烟断言（bind/close/isClosed） |
| testWhiteBox | JDK-8 成员表：4 构造器、bind/receive/选项族 synchronized、send 非 synchronized、无 supportedOptions |
| testWhiteBoxConstructors | 构造器参数类型表、直接接口仅 Closeable |
| testUnboundState | `new DatagramSocket(null)`：isBound=false/localPort=0/localAddress=wildcard/lsa=null/getPort=-1/inetAddress=null |
| testImplicitBind | 未绑定 send/receive 自动绑定 wildcard 并分配临时端口（receive 前设 SoTimeout） |
| testBindMatrix | bind(null)→wildcard/lsa 形态；已绑→SocketException；占用端口→BindException（ctor 与 bind 两个入口）；closed bind |
| testPortValidation | 构造器端口非法值 → IAE 精确 "port out of range:<v>"（双侧同措辞）；65535 合法；ctor(0,null) |
| testConnectMatrix | connect 状态/远端端点 equals 往返/重连/断开重置（port=-1/inet=null/rsa=null）；connect(lo,0) JDK-8 记录状态；connect 1.2.3.4 不发包；端口非法消息 "connect: 65536"（JDK-8 措辞）；unresolved → SocketException "Unresolved address" |
| testConnectFilteringAndDefaultTarget | 三 socket 回环：B connect A 后 C 的包被过滤（SocketTimeoutException）；A（已连 B）无地址 send 送达 B；未连接 receive 收任何人 |
| testSendReceiveSemantics | offset 窗口字节精确往返；坏端口包 → IAE "Port out of range:2147483647"（双侧同）；无地址包 → NPE（JDK-8）；send/receive(null) NPE；receive 超时精确 "Receive timed out"、超时不改包 |
| testOptionsMatrix | SoTimeout 默认 0/往返/0 合法/负 → IAE "timeout < 0"；SNDBUF/RCVBUF 默认>0、64k 请求精确生效、0/负 IAE；trafficClass 默认 0、0x28 往返、-1 IAE；broadcast 默认 true/往返；reuseAddress 未绑定往返 |
| testCloseStateMachine | close 后 send/receive/bind/get/setSoTimeout → SocketException；getLocalPort=-1、getLocalAddress=null、lsa=null；isBound 保持 true；connect on closed 不复活状态；close 幂等 |
| testSelfDatagramAndOrdering | 自发自收、3 包按序到达（回环确定性） |
| testEphemeralIndependence | 三构造入口临时端口互异、wildcard/loopback 形态 |
| testMiscExtras | 零长数据报、wildcard lsa、关闭前后 toString |
| testImplFactoryOneShot（文件尾） | 二次 setDatagramSocketImplFactory → SocketException "factory already defined"（精确）；null impl → new DatagramSocket NPE |

## 五、易错规范细节与版本差异

- JDK-8 的 bind/receive/setSoTimeout 等为 synchronized，send 不是；JDK-27 全部去 synchronized
  （static setDatagramSocketImplFactory 两侧保持 synchronized）。
- 未绑定 getLocalPort()==0；关闭后 getLocalPort()==-1、getLocalAddress()==null。
- 未绑定即 close：isBound 保持 false；已绑定再 close：isBound 保持 true。
- connect(InetAddress,int) 端口非法消息 JDK-8 为 "connect: <v>"，JDK-27 为 "port out of range:<v>"。
- send 无地址包在未连接 socket 上：JDK-8 NPE / JDK-27 IAE（类型不同，各侧断各的）。
- 关闭后异常消息大小写不一（"Socket is closed"/"Socket closed"）→ 只断 SocketException 类型。
