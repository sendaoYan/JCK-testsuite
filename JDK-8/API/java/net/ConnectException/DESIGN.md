# java.net.ConnectException 测试设计案（JDK-8）

> 批次 F 深化：层级/catch 矩阵、构造器矩阵、回环真实触发（拒绝端口连接）及失败后状态。

## 1. API 功能

- extends SocketException（→IOException）；构造器 `()`/`(String)`。
- 语义：连接尝试被对端拒绝（ECONNREFUSED）。

## 2. 测试用例设计（ConnectExceptionTest.java，59 断言）

- 构造器矩阵 + toString 精确形式 + serialVersionUID==3831404271622369215L（双 JDK 一致）+ 恰好 2 构造器。
- 层级：CE<SE<IOException 三级 catch；与 BE/STE/NRTH 互不为子类。
- cause：CE 可作为 CE 的 cause（重试级联模型）+ 消息可读；全套 cause/suppressed/序列化/跨线程。
- 真实触发（回环）：ServerSocket(0) 取端口即 close，connect 该端口→ConnectException "Connection refused (Connection refused)"（JDK-8 双写后缀）；第二次拒绝同样 CE；refused 后 **isClosed()==false**（JDK-8 特有）。

## 易错规范细节（已锁入断言）

- refused 后 JDK-8 socket 仍"未关闭"（可再 connect），JDK-27 则 isClosed=true——版本差异按侧断言。

## JDK-8 vs JDK-27 差异

- 消息后缀：JDK-8 "(Connection refused)" / JDK-27 "(connect failed)"；post-refusal isClosed：false/true（56 断言）。
