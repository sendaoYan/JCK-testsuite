# java.net.DatagramPacket 测试设计案（JDK-8）

> 覆盖目标：完整契约。6 个构造器、get/set 访问器、buffer/offset/length 边界校验（IAE/NPE）、
> 未解析地址拒绝（IAE）、setData(byte[]) 重置 offset、getSocketAddress 往返。全部离线。

## 1. API 功能

- 构造器（6）：`(byte[],int,int)`、`(byte[],int)`、`(byte[],int,int,InetAddress,int)`、
  `(byte[],int,InetAddress,int)`、`(byte[],int,int,SocketAddress)`、`(byte[],int,SocketAddress)`。
- 访问：`getAddress/getPort/getData/getOffset/getLength/getSocketAddress`。
- 修改：`setData(byte[]/byte[],int,int)`、`setAddress`、`setPort`、`setSocketAddress`、`setLength`。

## 2. 用户如何使用

- `new DatagramPacket(buf, len)` 接收；`new DatagramPacket(buf, off, len, addr, port)` 发送。
- `setData` 换缓冲；`setLength` 限制有效载荷。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/DatagramPacket.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/DatagramPacket/`（Constructor、CheckBoundaries、Offset、Unresolved、ReuseBuf）。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_net/DatagramPacket/`（CtorTests、set*Tests、SocketAddressTests）。

## 4. 测试用例设计（映射到 DatagramPacketTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 简单构造器 + 默认 offset/地址/端口 | testSimpleConstructors |
| P0 | 带地址构造器（4/6 参 + SocketAddress） | testAddressConstructors |
| P0 | set* 系列 + setData(byte[]) 重置 offset | testSetters |
| P0 | 边界矩阵（负长度/超长/溢出/null/负端口）→IAE/NPE | testBoundaries |
| P0 | 未解析地址/错误 SocketAddress 类型→IAE | testUnresolvedAndBadAddress |

## 易错规范细节（已锁入断言）

- `getData()` 返回**同一**缓冲引用（非拷贝）；`setData(byte[])` 重置 offset=0、length=缓冲长度。
- 长度/offset 越界、`Integer.MAX_VALUE` offset 溢出、负端口、端口>65535 → IllegalArgumentException；null 缓冲 → NPE。
- `setSocketAddress` 仅接受 InetSocketAddress；未解析地址 → IllegalArgumentException。
- JDK-8 特有：无地址包的 `getSocketAddress()` 重建 `InetSocketAddress(null,-1)` → IllegalArgumentException
  （JDK-27 已改为返回 wildcard 0.0.0.0:0，镜像时按各自 JDK 适配）。
