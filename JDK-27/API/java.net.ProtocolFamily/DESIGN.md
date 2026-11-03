# ProtocolFamily 设计文档

## 一、API 功能

标记接口，表示通信协议族(如 IPv4/IPv6/Unix)。

## 二、Java 用户如何使用

用户以 ProtocolFamily 作为通道创建参数类型。

## 三、开源代码实际用法调研

NIO 通道 API 使用此接口统一协议族。

## 四、测试用例设计

验证为接口；StandardProtocolFamily 实现之。
