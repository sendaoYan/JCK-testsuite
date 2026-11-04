# UnixDomainSocketAddress 设计文档

## 一、API 功能

Unix 域套接字地址，表示文件系统路径端点，用于本机进程间通信(JDK 16+)。

## 二、Java 用户如何使用

用户用 UnixDomainSocketAddress.of(path) 构造，传给 SocketChannel.open(StandardProtocolFamily.UNIX) 或 ServerSocketChannel.bind。

## 三、开源代码实际用法调研

本机高性能 IPC(如 Docker daemon、systemd)使用 Unix 域套接字。

## 四、测试用例设计

of/getPath/equals/hashCode/toString；不同路径不相等；继承 SocketAddress。
