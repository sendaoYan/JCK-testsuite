# java.net.SocketOptions 测试设计案（JDK-8）

> 批次 F 深化：14 个常量的精确值矩阵（反射断言）、接口方法签名、负例。

## 1. API 功能

- 常量：TCP_NODELAY=1、SO_BINDADDR=15、SO_REUSEADDR=4、SO_BROADCAST=32、IP_MULTICAST_IF=16、IP_MULTICAST_IF2=31、IP_MULTICAST_LOOP=18、IP_TOS=3、SO_LINGER=128、SO_TIMEOUT=0x1002(4102)、SO_SNDBUF=0x1001(4097)、SO_RCVBUF=0x1002(4098)、SO_KEEPALIVE=8、SO_OOBINLINE=4099。
- 方法：`setOption(int,Object)`、`getOption(int)`（均抛 SocketException）。

## 2. 测试用例设计（SocketOptionsTest.java，114 断言）

- 常量矩阵：每个常量按名反射取值并与上表精确比对；类型 int、static final、public。
- SO_REUSEPORT：JDK-8 **不存在**（NoSuchFieldException 断言；JDK 9 引入，值 14）。
- 接口白盒：两方法均抛 SocketException、非 abstract（接口默认实现约束）。
- Socket/DatagramSocket 实现 SocketOptions 的 instanceof 断言。

## 易错规范细节（已锁入断言）

- 4102/4097/4098/4099 四个高段值容易记混：SO_TIMEOUT=4102、SO_SNDBUF=4097、SO_RCVBUF=4098、SO_OOBINLINE=4099。

## JDK-8 vs JDK-27 差异

- JDK-27（132 断言）：15 常量，含 SO_REUSEPORT=14。
