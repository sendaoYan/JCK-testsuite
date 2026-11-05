# jdk.net.ExtendedSocketOptions 设计案（JDK-8）

## 1. API 功能
`jdk.net.ExtendedSocketOptions`（final 类，包私有构造，不可实例化）持有 JDK 特定/平台特定的扩展 `SocketOption` 常量。JDK-8 侧恰有 4 个 public static 字段：

| 常量 | name() | type() | 说明 |
|---|---|---|---|
| SO_FLOW_SLA | "SO_FLOW_SLA" | jdk.net.SocketFlow | Solaris 流优先级/带宽 SLA（Linux 不支持） |
| TCP_KEEPIDLE | "TCP_KEEPIDLE" | java.lang.Integer | TCP keepalive 空闲探测间隔（内核 TCP_KEEPIDLE） |
| TCP_KEEPINTERVAL | "TCP_KEEPINTERVAL" | java.lang.Integer | 探测包间隔（TCP_KEEPINTVL） |
| TCP_KEEPCOUNT | "TCP_KEEPCOUNT" | java.lang.Integer | 探测次数上限（TCP_KEEPCNT） |

JDK-8 无 `isSocketOptionSupported` 方法（JDK-9+ 移入内部 sun.net.ext；公共面以 `Sockets.supportedOptions(Class)` 为准）。

## 2. 使用方式
- 读取常量直接引用：`ExtendedSocketOptions.TCP_KEEPIDLE`。
- 设置/读取经 `jdk.net.Sockets.setOption(Socket, SocketOption, T)` / `getOption`（JDK-8 的 java.net.Socket 没有 SocketOption 版 setOption/getOption，Sockets 是唯一通道）。
- 未连接的 `new java.net.Socket()` 即可 set/get TCP_KEEP*（纯本地 setsockopt，不绑端口、不连接）。
- 支持面查询：`Sockets.supportedOptions(Socket.class)` 等。

## 3. 参考来源
- Oracle javadoc（Java SE 8, jdk.net 模块文档）。
- OpenJDK 源码：~/git/jdk8u-dev/jdk/src/share/classes/jdk/net/ExtendedSocketOptions.java、Sockets.java（initOptionSets：flowSupported 仅 Solaris 加 SO_FLOW_SLA；keepAliveOptions() 来自 sun.net.ExtendedOptionsHelper，Linux 提供 TCP_KEEP* 三项）。
- 实测记录（/tmp/probe-dnet/ProbeNet.java、ProbeUnwrap.java，jdk8u372-b07 @ Linux 5.15）：
  - TCP_KEEPIDLE=100 回读 100；TCP_KEEPINTERVAL=50 回读 50；TCP_KEEPCOUNT=8 回读 8。
  - SO_FLOW_SLA set → UnsupportedOperationException("SO_FLOW_SLA")（isSupported 先拒绝）。
  - TCP_KEEPIDLE=0/-5 → SocketException("Invalid argument")（内核拒绝）。
  - SO_RCVBUF=0 → SocketException("bad parameter for SO_SNDBUF or SO_RCVBUF")。
  - 错型值（String 传 Integer 选项）→ SocketException（同上消息，JDK-8 无值类型检查）。
  - 关闭后 set/get TCP_KEEP* → SocketException("Socket closed")（小写 c，扩展选项路径）；标准选项路径为 "Socket Closed"（大写 C）。

## 4. 测试设计 + 易错细节
**测试设计**（6→52 断言）：
1. 反射枚举 public static SocketOption 字段全集 == {SO_FLOW_SLA, TCP_KEEPIDLE, TCP_KEEPINTERVAL, TCP_KEEPCOUNT}（防止多/漏常量）。
2. 逐常量 name() 精确串 + type() 精确类；两两互异；hashCode 稳定；equals(null/String) false。
3. 类结构：final、无 public 构造器。
4. supportedOptions 平台面（Linux）：Socket/ServerSocket 含 TCP_KEEP*，DatagramSocket 不含；SO_FLOW_SLA 不在任何集合（Linux）。
5. 真实闭环：未连接 Socket 上 TCP_KEEPIDLE/KEEPINTERVAL/KEEPCOUNT set→get 精确回读 + 覆盖语义 + 返回值类型 instanceof。
6. 异常面：KEEPIDLE 0/-5 → SocketException；SO_FLOW_SLA set/get → UOE(name)；fake 选项 → UOE("FAKE_OPT")；SO_RCVBUF 0/错型 → SocketException；关闭 socket → SocketException；supported 集不可变、稳定、未知类型 → IAE("unknown socket type")。

**易错细节**：
- name() 恒等于字段名是 SocketOption 契约；反射枚举须过滤 `public static` 且类型 assignable 到 SocketOption（类里还有 static {} 等非字段成员）。
- JDK-8 通道是 Sockets.setOption/getOption，不要写 sock.setOption(SocketOption,...)——该方法 JDK-8 不存在（NoSuchMethod 断言反向锁死）。
- 关闭异常消息大小写：扩展选项路径 "Socket closed"，标准路径 "Socket Closed"，两条路径源码不同（SocketSecrets vs SocketImpl）。
- 反射调 setOption 时 invoke(null, socket, option, value)——第一参 null 是静态 receiver，socket 是实参；写反报 "wrong number of arguments"。

**8/27 差异表（ExtendedSocketOptions 常量）**

| 常量 | JDK-8 | JDK-27 |
|---|---|---|
| SO_FLOW_SLA (SocketFlow) | 有 | 删除（连类一起删） |
| TCP_KEEPIDLE/KEEPINTERVAL/KEEPCOUNT (Integer) | 有 | 有 |
| TCP_QUICKACK (Boolean) | 无 | 新增 |
| SO_INCOMING_NAPI_ID (Integer) | 无 | 新增（只读） |
| SO_PEERCRED (UnixDomainPrincipal) | 无 | 新增（unix-domain） |
| IP_DONTFRAGMENT (Boolean) | 无 | 新增（Linux inet socket 上不支持） |
