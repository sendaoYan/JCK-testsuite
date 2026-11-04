# SocketImplFactory 设计文档

## 一、API 功能

工厂接口，创建 SocketImpl 实例供 Socket/ServerSocket 使用。

## 二、Java 用户如何使用

用户实现 createSocketImpl() 并通过 Socket.setSocketImplFactory 注册。

## 三、开源代码实际用法调研

自定义套接字实现(如通过 SOCKS 或加密隧道)使用。

## 四、测试用例设计

验证为单抽象方法接口；实现返回 null/自定义实例的工厂并调用其 SAM 方法。
