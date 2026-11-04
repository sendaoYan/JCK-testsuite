# StandardSocketOptions 设计文档

## 一、API 功能

标准套接字选项枚举(SO_*、IP_*、TCP_* 等)，每个常量带名称与值类型。

## 二、Java 用户如何使用

用户用 StandardSocketOptions.SO_REUSEADDR 等作为 setOption/getOption 的键。

## 三、开源代码实际用法调研

NIO SocketChannel/ServerSocketChannel/DatagramChannel 与 java.net.Socket 通用。

## 四、测试用例设计

枚举常量存在性；name()/type() 正确；在真实 Socket 上 setOption/getOption 往返。

## 批次 F 深化补充（134 断言）

- 12 字段（JDK 9+ 含 SO_REUSEPORT）name/type/identity 矩阵：Boolean×5、Integer×5、NetworkInterface（IP_MULTICAST_IF）。
- JDK-8 侧（111 断言）：11 字段、SO_REUSEPORT 断言不存在。
- name() 精确串、type() 精确 Class、静态单例身份（同一字段两次引用 ==）。
