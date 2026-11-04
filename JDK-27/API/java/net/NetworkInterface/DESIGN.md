# NetworkInterface 设计文档

## 一、API 功能

表示网络接口(网卡)，可枚举接口、获取其 InetAddress 列表、MTU、是否回环/点对点/多播等。

## 二、Java 用户如何使用

用户用 NetworkInterface.getNetworkInterfaces()/getByName("lo")/getByInetAddress 遍历与查找接口。

## 三、开源代码实际用法调研

多网卡绑定、组播接口选择、网络监控工具普遍使用。

## 四、测试用例设计

枚举接口列表非空且自洽；getByName("lo") 为回环接口且 isLoopback；getByInetAddress(loopback) 返回回环接口；getName/isUp/isVirtual 自洽。

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
