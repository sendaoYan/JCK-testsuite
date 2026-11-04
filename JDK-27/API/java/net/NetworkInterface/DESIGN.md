# NetworkInterface 设计文档

## 一、API 功能

表示网络接口(网卡)，可枚举接口、获取其 InetAddress 列表、MTU、是否回环/点对点/多播等。

## 二、Java 用户如何使用

用户用 NetworkInterface.getNetworkInterfaces()/getByName("lo")/getByInetAddress 遍历与查找接口。

## 三、开源代码实际用法调研

多网卡绑定、组播接口选择、网络监控工具普遍使用。

## 四、测试用例设计

枚举接口列表非空且自洽；getByName("lo") 为回环接口且 isLoopback；getByInetAddress(loopback) 返回回环接口；getName/isUp/isVirtual 自洽。
