# Inet6Address 设计文档

## 一、API 功能

IPv6 地址(128 位)的表示，支持作用域 id、IPv4 兼容/映射地址等。

## 二、Java 用户如何使用

用户通过 getByAddress 或 getByName 得到 Inet6Address，判断 isIPv6Address()/isIPv4MappedAddress()。

## 三、开源代码实际用法调研

IPv6 部署的服务器与客户端按地址族区分处理时使用。

## 四、测试用例设计

getByAddress(16 字节) 返回 Inet6Address；isIPv6Address/isIPv4MappedAddress；getByAddress(null,null,0) 抛 UnknownHostException。
