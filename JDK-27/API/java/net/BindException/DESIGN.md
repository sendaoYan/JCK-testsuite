# BindException 设计文档

## 一、API 功能

套接字绑定失败(端口被占用/地址不可用)时抛出的 SocketException 子类。

## 二、Java 用户如何使用

用户在 ServerSocket/DatagramSocket bind 失败时捕获。

## 三、开源代码实际用法调研

端口冲突处理在服务器开发中常见。

## 四、测试用例设计

继承 SocketException/IOException；直接构造与带 message 构造；可作为 throws 声明。

## 批次 F 深化补充（55 断言）

- 层级 BE<SE<IOException、catch 矩阵、与 CE/STE/NRTH/PUE 互不为子类。
- serialVersionUID=-5945005768251722951L（双 JDK 一致）。
- 真实触发（回环）：占用 127.0.0.1 临时端口后重绑→"Address already in use"（JDK-27 无后缀；JDK-8 为 "…(Bind failed)"）；wildcard 重叠绑定同样 BindException；holder 关闭后端口可重绑。
- cause/suppressed/序列化/跨线程传播全套（BE 可作为 BE 的 cause）。
