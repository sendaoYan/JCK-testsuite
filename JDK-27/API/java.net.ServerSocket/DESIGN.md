# ServerSocket 设计文档

## 一、API 功能

TCP 服务端监听套接字，绑定本地端口并 accept() 接受连接。

## 二、Java 用户如何使用

用户 new ServerSocket(0) 绑定临时端口，getLocalPort() 获取端口，accept() 得到连接的 Socket，close() 释放。

## 三、开源代码实际用法调研

Web 服务器、RMI 注册表、应用服务器都基于 ServerSocket 监听。

## 四、测试用例设计

new ServerSocket(0) 临时端口、isBound/getLocalPort/getLocalSocketAddress、setSoTimeout/getSoTimeout、accept 后读写、close 后 accept 抛异常。
