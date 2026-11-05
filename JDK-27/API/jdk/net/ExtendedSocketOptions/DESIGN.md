# jdk.net.ExtendedSocketOptions 设计案（JDK-27）

## 1. API 功能
`jdk.net.ExtendedSocketOptions`（final 类，不可实例化）持有 7 个扩展 SocketOption 常量（JDK-27.0.1 实测面）：

| 常量 | name() | type() | 说明 |
|---|---|---|---|
| TCP_QUICKACK | "TCP_QUICKACK" | Boolean | TCP 快速 ACK，可读写 |
| TCP_KEEPIDLE | "TCP_KEEPIDLE" | Integer | keepalive 空闲间隔，可读写 |
| TCP_KEEPINTERVAL | "TCP_KEEPINTERVAL" | Integer | 探测间隔，可读写 |
| TCP_KEEPCOUNT | "TCP_KEEPCOUNT" | Integer | 探测次数，可读写 |
| SO_INCOMING_NAPI_ID | "SO_INCOMING_NAPI_ID" | Integer | **只读**（set → SocketException "Attempt to set read only option"） |
| SO_PEERCRED | "SO_PEERCRED" | jdk.net.UnixDomainPrincipal | unix-domain socket 对端凭证，只读 |
| IP_DONTFRAGMENT | "IP_DONTFRAGMENT" | Boolean | IP DF 位（Linux inet socket 上不支持 → UOE） |

无 `isSocketOptionSupported` 公共方法；包私有 `options()`；SO_FLOW_SLA 已随 SocketFlow 一并删除。

## 2. 使用方式
- JDK-9+ 双通道：`socket.setOption(ExtendedSocketOptions.TCP_KEEPIDLE, 100)`（java.net.Socket 原生方法）或 `jdk.net.Sockets.setOption(...)`（@Deprecated since 16，转发原生方法），两通道互通。
- 未连接 `new java.net.Socket()` 即可 set/get TCP_KEEP*/QUICKACK/NAPI_ID（本地 setsockopt）。
- 支持面：`socket.supportedOptions()` / `Sockets.supportedOptions(Socket.class)`。

## 3. 参考来源
- Oracle javadoc（JDK 27 jdk.net.ExtendedSocketOptions）。
- OpenJDK 源码：~/git/jdk/src/jdk.net/share/classes/jdk/net/ExtendedSocketOptions.java（常量定义与 @since 注解：QUICKACK since 10、SO_PEERCRED since 16、IP_DONTFRAGMENT since 16、NAPI_ID since 19...）；java.net.Socket 的 setOption/getOption/unsupportedOptions 实现（~/git/jdk/src/java.base/share/classes/java/net/Socket.java 等，值类型检查抛 IllegalArgumentException("Invalid value ...")、只读选项检查抛 SocketException("Attempt to set read only option ...")）。
- 实测记录（/tmp/probe-dnet/ProbeNet.java、ProbeUnwrap.java、Qa.java，jdk-27.0.1.0.2+1 @ Linux 5.15）：
  - 反射枚举恰 7 个 public static SocketOption 字段；SO_FLOW_SLA 字段不存在；Class.forName("jdk.net.SocketFlow") → ClassNotFoundException。
  - supportedOptions(Socket.class) 13 项（含 SO_REUSEPORT/TCP_QUICKACK/SO_INCOMING_NAPI_ID；无 SO_PEERCRED/IP_DONTFRAGMENT）；ServerSocket 9；DatagramSocket 6；MulticastSocket 8。
  - 未连接 socket：KEEPIDLE=100 回读 100、KEEPINTERVAL=50、KEEPCOUNT=8、QUICKACK true/false 双向回读；NAPI_ID get=0。
  - NAPI_ID set=5 → SocketException("Attempt to set read only option SO_INCOMING_NAPI_ID")。
  - SO_PEERCRED get（inet socket）→ UnsupportedOperationException("'SO_PEERCRED' not supported")；IP_DONTFRAGMENT set → UOE("'IP_DONTFRAGMENT' not supported")。
  - KEEPIDLE=0/-5 → SocketException("Invalid argument")。
  - 错型值 → IllegalArgumentException("Invalid value 'notanint'")；SO_RCVBUF=0 被接受（get 回内核最小值 1152）。
  - 原生通道 round-trip：s.setOption(SO_RCVBUF,16384) → s.getOption=16384 = Sockets.getOption；Sockets.setOption(KEEPIDLE,111) → s.getOption=111（互通）。
  - 关闭后 → SocketException("Socket is closed")。

## 4. 测试设计 + 易错细节
**测试设计**（9→93 断言）：
1. 反射枚举常量全集 == 7 名集合（点名差集：SO_FLOW_SLA 无、QUICKACK/NAPI_ID/PEERCRED/DONTFRAG 新增）。
2. 逐常量 name()/type() 精确断言（含 UnixDomainPrincipal 类型）。
3. 两两互异 + hashCode 稳定 + equals 负例。
4. 差集反向锁死：SO_FLOW_SLA 字段 NoSuchField、SocketFlow 类 CNFE。
5. 类结构 final/无公共构造。
6. 支持面矩阵：Socket 集含 5 个扩展项 + SO_REUSEPORT、不含 SO_PEERCRED/IP_DONTFRAGMENT；ServerSocket 含 QUICKACK；DatagramSocket 不含 KEEPIDLE。
7. 真实闭环：TCP_KEEP* 精确回读 + 覆盖 + 类型 instanceof；QUICKACK true/false 双向。
8. 只读/不支持异常面：NAPI_ID set → SocketException 含 "read only"；SO_PEERCRED get → UOE；IP_DONTFRAGMENT set → UOE；消息含选项名。
9. 内核非法值 KEEPIDLE 0/-5 → SocketException。
10. 值类型检查（27 独有）：错型 → IAE（含 "Invalid value"）。
11. SO_RCVBUF=0 接受（8 侧抛——差异断言）。
12. 双通道互通：原生 set → Sockets get；Sockets set → 原生 get。
13. 关闭 socket 三路（Sockets set/get、原生 get）→ SocketException("Socket is closed")。
14. 支持集不可变/稳定/未知类型 IAE。

**易错细节**：
- name() 恒等于字段名；反射枚举要过滤 public static 且 SocketOption 可赋型。
- JDK-27 编译 Sockets/NetworkPermission 会有 removal 警告，不是错误。
- 「未连接 socket 上 set TCP_KEEPIDLE」在两侧都可用（实测），无需连接或绑定；SO_PEERCRED 需真 unix-domain socket 且已连接才有值——本测试只断 inet socket 上的 UOE 面。
- NAPI_ID 在未连接 socket 上 get 返回 0（内核默认），不是异常——别把它写成 UOE 断言。

**8/27 差异表（常量与方法面）**

| 维度 | JDK-8 | JDK-27 |
|---|---|---|
| SO_FLOW_SLA + SocketFlow | 有 | 删除 |
| TCP_QUICKACK / SO_INCOMING_NAPI_ID / SO_PEERCRED / IP_DONTFRAGMENT | 无 | 有 |
| TCP_KEEP* (Integer) | 有 | 有 |
| 值类型检查 | 无（错型落内核 → SocketException） | IllegalArgumentException("Invalid value ...") |
| SO_RCVBUF=0 | SocketException | 接受 |
| 关闭消息 | "Socket Closed"/"Socket closed" | "Socket is closed" |
| 原生 Socket.setOption(SocketOption) | 无 | 有 |
