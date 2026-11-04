# InetAddress 设计文档

## 一、API 功能

表示 IP 地址(可含主机名)，提供名称解析、回环/本机地址、可达性检测等，是 Socket 地址的基础。

## 二、Java 用户如何使用

用户用 InetAddress.getByName/getByAddress/getLoopbackAddress 获取地址，用于创建 Socket/DatagramPacket。

## 三、开源代码实际用法调研

几乎所有网络库都用 InetAddress 作为端点地址；DNS 客户端与连接池亦大量使用。

## 四、测试用例设计

getByAddress(byte[]) 构造并验证 isLoopbackAddress/getAddress/equals/hashCode/toString；getByAddress(null, addr) 返回地址；getByName("localhost") 解析。
