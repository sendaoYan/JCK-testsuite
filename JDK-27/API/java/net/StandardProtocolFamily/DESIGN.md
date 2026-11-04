# StandardProtocolFamily 设计文档

## 一、API 功能

标准协议族枚举：INET、INET6、UNIX。

## 二、Java 用户如何使用

用户用 StandardProtocolFamily.INET/UNIX 作为通道打开的协议族参数。

## 三、开源代码实际用法调研

NIO 通道编程按协议族创建 SocketChannel/ServerSocketChannel。

## 四、测试用例设计

枚举值 INET/INET6/UNIX 存在且顺序/name 正确；valueOf 往返；实现 ProtocolFamily。

## 批次 F 深化补充（65 断言）

- 枚举结构：final enum、直接接口恰好 {java.net.ProtocolFamily}、values()/valueOf(String)/name() 存在。
- 常量集 {INET=0, INET6=1, UNIX=2}（JDK 16+）；name()/toString() 精确串且一致。
- valueOf 边界：BOGUS/空串/小写→IAE（消息含常量名）、null→NPE。
- compareTo 全序：INET<INET6<UNIX；equals 身份、hashCode 稳定、getDeclaringClass。
- values() 防御拷贝：两次调用数组不同、改副本不影响枚举。
- 序列化恒等：round-trip 返回同一单例。
- `SocketChannel.open(INET)`、`ServerSocketChannel.open(UNIX)` 家族驱动用法。
