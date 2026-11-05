# jdk.net.Sockets 设计案（JDK-8）

## 1. API 功能
`jdk.net.Sockets`（public class，私有构造）为三类 java.net socket 提供 SocketOption 版 set/get 与支持面查询：
- `<T> void setOption(Socket, SocketOption<T>, T)` / `<T> T getOption(Socket, SocketOption<T>)`
- ServerSocket、DatagramSocket（含 MulticastSocket，按实际类型路由）同签名 4 方法。
- `Set<SocketOption<?>> supportedOptions(Class<?>)`：返回不可变集。

JDK-8 的 java.net.Socket **没有** setOption/getOption(SocketOption) 公共方法（javap 确认），Sockets 是唯一通道（内部反射进 java.net.SocketSecrets）。

## 2. 使用方式
```java
Socket s = new Socket();                       // 未连接即可，本地操作
Sockets.setOption(s, StandardSocketOptions.SO_RCVBUF, 65536);
int v = Sockets.getOption(s, StandardSocketOptions.SO_RCVBUF);
Set<SocketOption<?>> opts = Sockets.supportedOptions(Socket.class);
```
不支持的选项先经 isSupported 检查抛 UnsupportedOperationException(选项名)，再进内核。

## 3. 参考来源
- Oracle javadoc（Java SE 8 jdk.net.Sockets）。
- OpenJDK 源码：~/git/jdk8u-dev/jdk/src/share/classes/jdk/net/Sockets.java（initOptionSets：Socket=7 标准+flow+keepAliveOptions；ServerSocket 无 SNDBUF/NODELAY；DatagramSocket 无 TCP 系；MulticastSocket 加 IP_MULTICAST 三项；supportedOptions 未知类型 IAE("unknown socket type")；集 unmodifiable）。
- 实测记录（/tmp/probe-dnet/ProbeNet.java、ProbeUnwrap.java，jdk8u372-b07 @ Linux 5.15）：
  - Socket 集恰 10 项：7 标准 + TCP_KEEP*（Linux）；无 SO_FLOW_SLA。ServerSocket 6、DatagramSocket 4、MulticastSocket 7。
  - SO_RCVBUF=65536 精确回读；SO_SNDBUF=32768 精确回读；TCP_NODELAY true/false、SO_REUSEADDR true/false、SO_KEEPALIVE on/off 全可逆。
  - setOption(s, null, v) / getOption(s, null) → NPE；setOption((Socket)null, ...) → IllegalArgumentException（JDK-8 反射通道实测；JDK-27 为 NPE——差异）。
  - 关闭后 set/get → SocketException("Socket Closed")（标准选项路径）。
  - 不支持选项（Socket 上的 IP_MULTICAST_TTL、ServerSocket 上的 TCP_NODELAY）→ UnsupportedOperationException(选项名)。
  - SO_RCVBUF=0 → SocketException("bad parameter for SO_SNDBUF or SO_RCVBUF")（JDK-27 接受 0——差异）。
  - 错型值（String 传 Integer 选项）→ SocketException（JDK-8 无类型检查；JDK-27 为 IllegalArgumentException("Invalid value ...")——差异）。
  - ServerSocket SO_RCVBUF 回读可能被内核取整（断言用弱 >= 1024）。

## 4. 测试设计 + 易错细节
**测试设计**（4→57 断言）：
1. 真实闭环矩阵（未连接 Socket）：SO_RCVBUF/SO_SNDBUF 精确回读；SO_REUSEADDR/TCP_NODELAY/SO_KEEPALIVE 双向翻转；值类型 instanceof Integer/Boolean。
2. 扩展选项 TCP_KEEPIDLE 经 Sockets 通道闭环。
3. null 参数矩阵：null 选项 NPE；null socket IAE（JDK-8 通道特性）。
4. 不支持选项 UOE + 消息为选项名。
5. 关闭 socket → SocketException("Socket Closed")（set/get 双向 + 消息串）。
6. supportedOptions 四类 socket 矩阵：必含项逐个点名 + 必不含项（ServerSocket 无 TCP_NODELAY/SO_SNDBUF；DatagramSocket 无 TCP 系/KEEPIDLE）；集不可变（add → UOE）、跨调用稳定；String.class/null → IAE("unknown socket type")。
7. ServerSocket(0) 闭环：SO_RCVBUF 弱断言、SO_REUSEADDR 精确、TCP_NODELAY UOE、getLocalPort>0，finally close。
8. DatagramSocket/MulticastSocket(null addr) 闭环：IP_MULTICAST_TTL 精确回读、LOOP 翻转。
9. 方法面：java.net.Socket 无 setOption(SocketOption)（反射 NoSuchMethod 锁死）；Sockets 不可实例化。

**易错细节**：
- JDK-8 的 StandardSocketOptions 无 SO_REUSEPORT（JDK-9 才加），断言里不能引用该常量。
- 「Socket 上的 IP_MULTICAST_TTL」是不支持选项的正例（在 StandardSocketOptions 里但不在该 socket 的支持集）。
- ServerSocket SO_RCVBUF 回读值受内核影响（取整/翻倍），只能弱断言；Socket 的 SO_RCVBUF/SO_SNDBUF 实测精确回读，可精确断言。
- 关闭消息大小写两路径不同：标准 "Socket Closed" vs 扩展 "Socket closed"。
- MulticastSocket/DatagramSocket 用 `(SocketAddress) null` 构造避免绑端口。

**8/27 差异表（Sockets）**

| 维度 | JDK-8 | JDK-27 |
|---|---|---|
| 实现机制 | 反射 SocketSecrets | 直接转发 sock.setOption/getOption |
| java.net.Socket.setOption/getOption(SocketOption) | 无 | 有（JDK-9+ 标准 API） |
| setOption(null socket) | IllegalArgumentException | NullPointerException |
| 关闭消息 | "Socket Closed" | "Socket is closed" |
| 错型值 | SocketException（内核拒绝） | IllegalArgumentException("Invalid value ...") |
| SO_RCVBUF=0 | SocketException | 接受（get 回内核最小值） |
| @Deprecated | 无 | @Deprecated(since="16") |
| 支持集 | 无 SO_REUSEPORT/TCP_QUICKACK/NAPI_ID | 含 SO_REUSEPORT/TCP_QUICKACK/SO_INCOMING_NAPI_ID |
