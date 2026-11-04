# InterfaceAddress 设计文档

## 一、API 功能

网络接口上绑定的地址及其子网前缀长度、广播地址。

## 二、Java 用户如何使用

用户从 NetworkInterface.getInterfaceAddresses() 获取接口地址，读取 getAddress/getBroadcast/getNetworkPrefixLength。

## 三、开源代码实际用法调研

子网计算、网络配置工具使用。

## 四、测试用例设计

遍历 NetworkInterface 的 getInterfaceAddresses()，断言 getAddress 非空、getNetworkPrefixLength>=0、广播地址为 null 或与地址族一致。

## 深化补充（批次 E）

### 缺口分析

原文件仅 4 段概览 + 少量断言（Inet4Address 26 / Inet6Address 20 / UnknownHostException 8 /
NetworkInterface 41 / InterfaceAddress 48），缺：精确负例消息、字面量/文本归一化矩阵、
scope 语义、equals 跨构造、序列化类型保持、回环接口确定性结构断言等维度。

### 新增维度（详见 JDK-8 侧同名 DESIGN.md 的四部分设计案，行为双侧一致）

- 字面量→具体类型断言、字节往返、分类/MC 范围矩阵、hashCode 精确值、序列化类型保持。
- JDK-27 专属：Inet4Address.ofLiteral 严格矩阵 + ofPosixLiteral（127.1→127.0.0.1、010→八进制）；
  Inet6Address.ofLiteral 拒绝 IPv4 字面量→IAE；NetworkInterface 流式视图与枚举一致性；
  InterfaceAddress final 类白盒。
- 断言数：Inet4Address 26→135，Inet6Address 20→96，UnknownHostException 8→23，
  NetworkInterface 41→89，InterfaceAddress 48→74。
