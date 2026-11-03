# SocketException 设计文档

## 一、API 功能

套接字操作失败的通用异常。

## 二、Java 用户如何使用

用户在所有 socket 操作处捕获 IOException/SocketException。

## 三、开源代码实际用法调研

网络编程最常用的异常之一。

## 四、测试用例设计

继承 IOException；构造器；closed socket 操作触发。
