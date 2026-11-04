# UnknownHostException 设计文档

## 一、API 功能

主机名无法解析为 IP 地址时抛出的异常。

## 二、Java 用户如何使用

用户在 InetAddress.getByName 失败时捕获。

## 三、开源代码实际用法调研

DNS 解析失败的通用处理。

## 四、测试用例设计

继承 IOException；构造器；getByAddress(null) 触发。
