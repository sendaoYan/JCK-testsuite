# jdk.net.Sockets 设计案

## 1. API 功能
Socket 选项工具：setOption/getOption/supportedOptions。

## 2. Java 用户如何使用
Sockets.setOption(socket, option, value)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.net/share/classes/jdk/net/Sockets.java；test/jdk/jdk/net/。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 Socket/ServerSocket set/get option。

---

## 深化(2026-11-05)

### API 功能（深化后全貌）
- `@Deprecated(since="16")`：全部静态方法转发到 java.net.Socket/ServerSocket/DatagramSocket 的原生 setOption/getOption（JDK-9+ 标准 API）。
- supportedOptions(Class)：Socket/ServerSocket/DatagramSocket/MulticastSocket 四类映射，返回不可变集，未知类型 IllegalArgumentException("unknown socket type")。

### 实测记录（/tmp/probe-dnet/ProbeNet.java、ProbeUnwrap.java，jdk-27.0.1.0.2+1 @ Linux 5.15）
- 支持集：Socket 13 项（含 SO_REUSEPORT、TCP_QUICKACK、TCP_KEEP*、SO_INCOMING_NAPI_ID；不含 SO_PEERCRED/IP_DONTFRAGMENT）；ServerSocket 9；DatagramSocket 6；MulticastSocket 8。
- 闭环：SO_RCVBUF 65536 / SO_SNDBUF 32768 精确回读；SO_REUSEPORT/TCP_NODELAY/SO_KEEPALIVE 双向翻转；TCP_KEEPIDLE=100、QUICKACK true/false。
- 双通道互通：Sockets.setOption ↔ socket.getOption 等值。
- 异常面：null 选项 NPE；**null socket NPE**（JDK-8 为 IllegalArgumentException）；错型值 IllegalArgumentException("Invalid value 'notanint'")（JDK-8 落内核 SocketException）；SO_RCVBUF=0 被接受（JDK-8 抛）；关闭后 SocketException("Socket is closed")（JDK-8 消息 "Socket Closed"）；不支持选项 UOE 消息含选项名。

### 测试设计（9→70 断言）
真实闭环矩阵（标准+扩展+SO_REUSEPORT）/ 双通道互通 / null 参数矩阵（含 27 侧 null socket NPE 差异点名）/ 错型 IAE 精确消息 / SO_RCVBUF=0 接受 / IP_DONTFRAGMENT UOE / 关闭异常三路 / 四类 socket 支持面逐项点名 + 负例 / 集不可变稳定 / 未知与 null 类型 IAE / ServerSocket+DatagramSocket+MulticastSocket 闭环（finally close）/ 原生方法面存在性 + Sockets @Deprecated + 不可实例化。

### 易错细节 + 8/27 差异
- ServerSocket SO_RCVBUF 回读受内核取整影响，用弱断言 >= 1024。
- DatagramSocket/MulticastSocket 用 `(SocketAddress) null` 构造避免绑端口。
- 差异表：见 JDK-8 侧 Sockets DESIGN 文末（方法面/异常面/消息/支持集五维对照）。
