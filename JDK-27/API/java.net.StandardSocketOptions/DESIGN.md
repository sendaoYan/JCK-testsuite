# StandardSocketOptions 设计文档

## 一、API 功能

标准套接字选项枚举(SO_*、IP_*、TCP_* 等)，每个常量带名称与值类型。

## 二、Java 用户如何使用

用户用 StandardSocketOptions.SO_REUSEADDR 等作为 setOption/getOption 的键。

## 三、开源代码实际用法调研

NIO SocketChannel/ServerSocketChannel/DatagramChannel 与 java.net.Socket 通用。

## 四、测试用例设计

枚举常量存在性；name()/type() 正确；在真实 Socket 上 setOption/getOption 往返。
