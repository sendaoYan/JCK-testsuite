# java.net.StandardSocketOptions 测试设计案（JDK-8）

> 批次 F 深化：11 个标准选项的 name/type/identity 矩阵、自定义 SocketOption 对照。

## 1. API 功能

- 常量（`SocketOption<T>` 单例）：SO_KEEPALIVE(Boolean)、SO_SNDBUF(Integer)、SO_RCVBUF(Integer)、SO_REUSEADDR(Boolean)、SO_LINGER(Integer)、IP_TOS(Integer)、IP_MULTICAST_IF(NetworkInterface)、IP_MULTICAST_TTL(Integer)、IP_MULTICAST_LOOP(Boolean)、TCP_NODELAY(Boolean)、SO_BROADCAST(Boolean)。
- JDK 9 增 SO_REUSEPORT(Boolean)——JDK-8 无。

## 2. 测试用例设计（StandardSocketOptionsTest.java，111 断言）

- checkNameType 矩阵：每个字段 name() 精确串 + type() 精确 Class + 静态单例身份（两次引用 ==）。
- SO_REUSEPORT：反射断言字段不存在。
- 名字唯一性：11 个 name() 互异。
- 与 SocketOptions（int 形）新旧两套 API 的对偶关系说明性断言。

## 易错规范细节（已锁入断言）

- IP_MULTICAST_IF 的 type 是 NetworkInterface 而非 InetAddress。
- SO_LINGER 为 Integer（-1 关闭），非 Boolean。

## JDK-8 vs JDK-27 差异

- JDK-27（134 断言）：12 字段，SO_REUSEPORT 存在且 Boolean。
