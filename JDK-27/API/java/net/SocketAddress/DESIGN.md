# SocketAddress 设计文档

## 一、API 功能

抽象基类，表示套接字端点地址，不绑定协议，序列化支持。

## 二、Java 用户如何使用

用户以 SocketAddress 类型传递端点(如 Socket.connect(SocketAddress))。

## 三、开源代码实际用法调研

框架以 SocketAddress 抽象统一 IPv4/IPv6/Unix 域端点。

## 四、测试用例设计

验证为 abstract 且实现 Serializable；子类 instanceof 判断；无抽象方法。

## 批次 F 深化补充（52 断言）

- 抽象值类型契约：自定义 PlainAddress 实现；InetSocketAddress 为平台实现。
- 序列化 round-trip：null message 构造、未解析地址（hostname 形式）保持未解析语义。
- bind(SocketAddress) 拒绝外来实现：SocketException "Unsupported address type"。
- equals/hashCode 对 InetSocketAddress 的端口+地址组合语义。
- JDK-27：`UnixDomainSocketAddress`（JDK 16+）equals 按 path、同 path 实例可相等、非 InetAddress 家族。
