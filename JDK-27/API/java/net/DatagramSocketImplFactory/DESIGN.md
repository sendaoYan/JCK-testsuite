# DatagramSocketImplFactory 设计文档

## 一、API 功能

工厂接口，创建 DatagramSocketImpl 实例供 DatagramSocket 使用。

## 二、Java 用户如何使用

用户实现 createDatagramSocketImpl() 并通过 DatagramSocket.setDatagramSocketImplFactory 注册。

## 三、开源代码实际用法调研

自定义 UDP 实现使用。

## 四、测试用例设计

验证为单抽象方法接口；实现并调用其 SAM 方法。
