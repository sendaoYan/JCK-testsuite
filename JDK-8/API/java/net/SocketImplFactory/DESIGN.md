# java.net.SocketImplFactory 测试设计案（JDK-8）

> 批次 F 深化：一次性安装语义、工厂调用时机、与 Socket/ServerSocket 的联动。

## 1. API 功能

- 函数式接口：`SocketImpl createSocketImpl()`。
- 安装点：`Socket.setSocketImplFactory(SocketImplFactory)`、`ServerSocket.setSocketFactory(SocketImplFactory)`——均为 JVM 全局一次性。

## 2. 测试用例设计（SocketImplFactoryTest.java，35 断言）

- 接口白盒：单方法、无默认方法。
- 计数工厂：每次 `new Socket()`/`new ServerSocket()` 触发一次 createSocketImpl（计数 + 身份断言）。
- 二次安装→SocketException "factory already defined"（Socket 与 ServerSocket 两条路径分别断言）。
- 注册后新 socket 走自定义实现：行为仍满足公共 API（getInputStream 可得、close 幂等）。
- 执行顺序：工厂注册放在 main **最后**（全局状态一次性，避免污染同 JVM 其他用例）。

## 易错规范细节（已锁入断言）

- 工厂只能设置一次；即使传 null 的二次调用也拒绝。
- factory 对已存在的 socket 无影响，只作用于之后的新建。

## JDK-8 vs JDK-27 差异

- JDK-27（31 断言）契约一致；仅默认 SocketImpl 不同（NioSocketImpl），工厂替换行为相同。
