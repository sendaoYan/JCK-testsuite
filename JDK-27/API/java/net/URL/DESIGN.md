# URL 设计文档

## 一、API 功能

表示统一资源定位符并绑定协议处理器(URLStreamHandler)，可 openConnection/openStream 访问资源，是 URI 的网络化扩展。

## 二、Java 用户如何使用

用户用 new URL(spec) 构造，调用 getProtocol/getHost/getPort/getPath/getFile/openConnection() 获取 URLConnection，或 openStream() 直接读字节。

## 三、开源代码实际用法调研

JDK 内建类加载、RMI、JDBC 驱动发现等大量使用 URL；很多框架用 URLClassLoader 从 URL 加载类。

## 四、测试用例设计

构造 http/https/file URL 断言组件；非法端口(非数字)抛 MalformedURLException；equals/hashCode 按相同 handler+资源判断；getDefaultPort 与 protocol 对应。
