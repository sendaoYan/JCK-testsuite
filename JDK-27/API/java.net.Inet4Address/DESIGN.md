# Inet4Address 设计文档

## 一、API 功能

IPv4 地址(32 位)的表示，继承 InetAddress。

## 二、Java 用户如何使用

用户通过 InetAddress.getByAddress 得到 Inet4Address 实例，判断 isIPv4Address() 与 isMulticastAddress()。

## 三、开源代码实际用法调研

网络编程中按 IPv4 地址族区分端点时使用。

## 四、测试用例设计

getByAddress(4 字节) 返回 Inet4Address；isIPv4Address/isAnyLocalAddress/isLoopbackAddress；instanceof InetAddress。
