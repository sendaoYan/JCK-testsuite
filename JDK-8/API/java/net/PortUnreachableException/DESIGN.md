# java.net.PortUnreachableException 测试设计案（JDK-8）

> 批次 F 深化：层级/catch 矩阵、构造器矩阵、回环 UDP 真实触发（ICMP Port Unreachable）。

## 1. API 功能

- extends SocketException（→IOException）；构造器 `()`/`(String)`。
- 语义：UDP 数据报抵达了不可达端口（ICMP Port Unreachable 回传到已连接的 DatagramSocket）。

## 2. 测试用例设计（PortUnreachableExceptionTest.java，57 断言）

- 构造器矩阵 + toString 精确形式 + serialVersionUID==8462541992376507323L（双 JDK 一致）+ 恰好 2 构造器。
- 层级：PUE<SE<IOException 三级 catch；与 BE/CE/STE/NRTH 互不为子类。
- cause：PUE 可作为 PUE 的 cause；全套 cause/suppressed/序列化/跨线程。
- 真实触发（回环 UDP）：DatagramSocket 绑 127.0.0.1 临时端口后 close；另一 socket `connect` 该地址、send 一字节、receive（SoTimeout 500 有界）→PortUnreachableException，JDK-8 消息 "ICMP Port Unreachable"。Linux 回环对已连接 UDP socket 投递 ICMP，探测 3/3 确定性。

## 易错规范细节（已锁入断言）

- 触发前提：DatagramSocket 必须 connect（否则 ICMP 不回投，receive 只会超时）。

## JDK-8 vs JDK-27 差异

- 触发消息：JDK-8 "ICMP Port Unreachable" / JDK-27 null（54 断言，按侧断言）。
