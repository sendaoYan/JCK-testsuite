# java.net.SocketImpl 测试设计案（JDK-8）

> 批次 F 深化：抽象骨架面（方法 + protected 字段）、最小子类 MiniImpl 的默认行为、基类抛错语义。

## 1. API 功能

- 抽象方法（public abstract）：`create(boolean)`、`connect(String,int)`、`connect(InetAddress,int)`、`connect(SocketAddress,int)`、`bind(InetAddress,int)`、`listen(int)`、`accept(SocketImpl)`、`getInputStream()`、`getOutputStream()`。
- 可覆写钩子：`close()`、`shutdownInput()`、`shutdownOutput()`、`available()`、`getOption/setOption(int,…)`、`supportsUrgentData()`、`sendUrgentData(int)`、`toString()`。
- protected 字段：`address`(InetAddress)、`port`(int)、`localport`(int)、`fd`(FileDescriptor)、`serverSocket`(ServerSocket)。

## 2. 测试用例设计（SocketImplTest.java，96 断言）

- 白盒：抽象方法数与修饰符逐一断言；protected 字段存在且类型精确。
- MiniImpl 子类：覆写 create/connect/bind/listen/accept/streams 为记录桩，调用后记录参数。
- 基类默认行为：`shutdownInput/shutdownOutput` 抛 IOException "Method not implemented!"（未覆写路径）。
- `toString()` 默认 "Socket[addr=null,port=0,localport=0]"（字段未设置时）。
- 字段默认值：port=0、localport=0、address=null、fd 可为 null。

## 易错规范细节（已锁入断言）

- SocketImpl 是 abstract，不能直接实例化；其抽象面即自定义传输实现的最小契约。
- 基类非抽象钩子的默认实现多为抛 IOException，"Method not implemented!" 是双 JDK 一致的字面量。

## JDK-8 vs JDK-27 差异

- JDK-27（79 断言）另有 JDK 13+ 的 protected `setOption(SocketOption,Object)`/`getOption(SocketOption)` 存在性断言。
