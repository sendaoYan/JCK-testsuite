# SocketTimeoutException 设计文档

## 一、API 功能

套接字读/写/accept 超时时抛出的 InterruptedIOException 子类。

## 二、Java 用户如何使用

用户在 setSoTimeout 后 receive/read 超时捕获。

## 三、开源代码实际用法调研

客户端超时控制与重试。

## 四、测试用例设计

继承 InterruptedIOException；构造器；DatagramSocket 超时 receive 触发。
