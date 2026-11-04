# Socket 设计文档

## 一、API 功能

TCP 客户端套接字，支持连接、读写、选项、半关闭与 SocketChannel 转换。

## 二、Java 用户如何使用

用户 new Socket(host, port) 或 new Socket()+connect()，用 getInputStream/getOutputStream 读写，finally 中 close()。

## 三、开源代码实际用法调研

所有 TCP 客户端(HTTP、数据库驱动、消息队列)底层都使用 Socket。

## 四、测试用例设计

localhost+临时端口握手：connect/accept、读写字节、TcpNoDelay/SoTimeout/KeepAlive/ReuseAddress/缓冲选项、shutdownOutput、close 后异常。
