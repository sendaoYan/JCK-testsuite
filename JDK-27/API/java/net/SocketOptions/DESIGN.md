# SocketOptions 设计文档

## 一、API 功能

遗留的套接字选项常量接口(SO_* 常量与 setOption/getOption(int,Object))。

## 二、Java 用户如何使用

遗留代码以 SocketImpl 实现此接口配置 socket 选项。

## 三、开源代码实际用法调研

JDK 早期 socket 实现使用；已被 StandardSocketOptions 取代但保留兼容。

## 四、测试用例设计

验证为接口并含 SO_TIMEOUT/SO_RCVBUF/SO_SNDBUF/SO_REUSEADDR 等常量；声明 setOption/getOption 方法。

## 批次 F 深化补充（132 断言）

- 常量精确值矩阵（反射断言）：TCP_NODELAY=1、SO_BINDADDR=15、SO_REUSEADDR=4、SO_BROADCAST=32、IP_MULTICAST_IF=16、IP_MULTICAST_IF2=31、IP_MULTICAST_LOOP=18、IP_TOS=3、SO_LINGER=128、SO_TIMEOUT=0x1002=4102、SO_SNDBUF=0x1001=4097、SO_RCVBUF=0x1002 修正为 4098、SO_KEEPALIVE=8、SO_OOBINLINE=4099、SO_REUSEPORT=14（JDK 9+，JDK-8 断言不存在→NoSuchFieldException）。
- 接口方法签名：setOption(int,Object) 抛 SocketException、getOption(int)、getOptionName()。
