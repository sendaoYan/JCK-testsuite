# SocketOptions 设计文档

## 一、API 功能

遗留的套接字选项常量接口(SO_* 常量与 setOption/getOption(int,Object))。

## 二、Java 用户如何使用

遗留代码以 SocketImpl 实现此接口配置 socket 选项。

## 三、开源代码实际用法调研

JDK 早期 socket 实现使用；已被 StandardSocketOptions 取代但保留兼容。

## 四、测试用例设计

验证为接口并含 SO_TIMEOUT/SO_RCVBUF/SO_SNDBUF/SO_REUSEADDR 等常量；声明 setOption/getOption 方法。
