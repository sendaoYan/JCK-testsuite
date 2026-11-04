# java.net.BindException 测试设计案（JDK-8）

> 批次 F 深化：层级/catch 矩阵、构造器矩阵、回环真实触发（占用端口重绑）。

## 1. API 功能

- extends SocketException（→IOException）；构造器 `()`/`(String)`。
- 语义：本地地址/端口绑定失败（EADDRINUSE）。

## 2. 测试用例设计（BindExceptionTest.java，56 断言）

- 构造器矩阵 + toString 精确形式 + serialVersionUID==-5945005768251722951L（双 JDK 一致）+ 恰好 2 构造器。
- 层级：BE<SE<IOException 三级 catch；与 CE/STE/PUE 互不为子类；equals 身份。
- cause：BE 可作为 BE 的 cause；全套 cause/suppressed/序列化/跨线程。
- 真实触发（回环）：`ServerSocket(0,50,127.0.0.1)` 占端口后同地址重绑→BindException "Address already in use (Bind failed)"（JDK-8 带后缀）；wildcard 重叠绑定同样 BE；holder 关闭后端口立即可重绑。

## 易错规范细节（已锁入断言）

- JDK-8 消息带 " (Bind failed)" 后缀，JDK-27 无后缀——共同前缀 "Address already in use"。

## JDK-8 vs JDK-27 差异

- 触发消息：JDK-8 "Address already in use (Bind failed)" / JDK-27 "Address already in use"（55 断言）。
