# Proxy 设计文档

## 一、API 功能

表示代理设置：类型(DIRECT/HTTP/SOCKS)与可选地址。

## 二、Java 用户如何使用

用户用 Proxy.NO_PROXY 或 new Proxy(Proxy.Type.HTTP, addr) 传给 Socket/URL.openConnection。

## 三、开源代码实际用法调研

需要代理访问外网的客户端使用。

## 四、测试用例设计

NO_PROXY 为 DIRECT 且 address==null；Type 枚举；构造/equals/hashCode/toString。
