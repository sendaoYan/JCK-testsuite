# InterfaceAddress 设计文档

## 一、API 功能

网络接口上绑定的地址及其子网前缀长度、广播地址。

## 二、Java 用户如何使用

用户从 NetworkInterface.getInterfaceAddresses() 获取接口地址，读取 getAddress/getBroadcast/getNetworkPrefixLength。

## 三、开源代码实际用法调研

子网计算、网络配置工具使用。

## 四、测试用例设计

遍历 NetworkInterface 的 getInterfaceAddresses()，断言 getAddress 非空、getNetworkPrefixLength>=0、广播地址为 null 或与地址族一致。
