# DatagramSocketImpl 设计文档

## 一、API 功能

UDP 套接字实现的抽象基类，封装 create/bind/send/receive/join/leave 等原生操作。

## 二、Java 用户如何使用

自定义 UDP 实现时继承并实现各方法，通过 setDatagramSocketImplFactory 注入。

## 三、开源代码实际用法调研

JDK 内建 DatagramSocketImpl 派生自此类；自定义 UDP 协议栈使用。

## 四、测试用例设计

验证 abstract 且实现 SocketOptions；声明的方法与 localPort/fd 字段；通过真实 DatagramSocket(0) 验证端口分配。
