# BindException 设计文档

## 一、API 功能

套接字绑定失败(端口被占用/地址不可用)时抛出的 SocketException 子类。

## 二、Java 用户如何使用

用户在 ServerSocket/DatagramSocket bind 失败时捕获。

## 三、开源代码实际用法调研

端口冲突处理在服务器开发中常见。

## 四、测试用例设计

继承 SocketException/IOException；直接构造与带 message 构造；可作为 throws 声明。
