# SocketImplFactory 设计文档

## 一、API 功能

工厂接口，创建 SocketImpl 实例供 Socket/ServerSocket 使用。

## 二、Java 用户如何使用

用户实现 createSocketImpl() 并通过 Socket.setSocketImplFactory 注册。

## 三、开源代码实际用法调研

自定义套接字实现(如通过 SOCKS 或加密隧道)使用。

## 四、测试用例设计

验证为单抽象方法接口；实现返回 null/自定义实例的工厂并调用其 SAM 方法。

## 批次 F 深化补充（31 断言）

- 一次性安装：第二次 `Socket.setSocketImplFactory`/`ServerSocket.setSocketFactory`→SocketException "factory already defined"。
- 工厂在每次 `new Socket()`/`new ServerSocket()` 时被调用一次（计数断言）。
- 已安装后再设 null factory 也拒绝。
- 测试顺序：factory 注册放在 main 最后（JVM 全局一次性状态，避免污染同 JVM 其他用例）。
