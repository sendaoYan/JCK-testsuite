# ProtocolFamily 设计文档

## 一、API 功能

标记接口，表示通信协议族(如 IPv4/IPv6/Unix)。

## 二、Java 用户如何使用

用户以 ProtocolFamily 作为通道创建参数类型。

## 三、开源代码实际用法调研

NIO 通道 API 使用此接口统一协议族。

## 四、测试用例设计

验证为接口；StandardProtocolFamily 实现之。

## 批次 F 深化补充（32 断言）

- 接口白盒：public interface、恰好 1 个声明方法 name()（abstract、返回 String、无参）。
- 自定义实现：任意名称（含 null/空串）透传、同名单实现仍是不同对象。
- 平台实现：INET/INET6/UNIX（JDK 16+ 三常量）经接口调用 name()。
- 多态使用矩阵：标准 + 自定义混排。
