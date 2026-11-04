# SocketTimeoutException 设计文档

## 一、API 功能

套接字读/写/accept 超时时抛出的 InterruptedIOException 子类。

## 二、Java 用户如何使用

用户在 setSoTimeout 后 receive/read 超时捕获。

## 三、开源代码实际用法调研

客户端超时控制与重试。

## 四、测试用例设计

继承 InterruptedIOException；构造器；DatagramSocket 超时 receive 触发。

## 批次 F 深化补充（57 断言）

- 关键分叉：extends InterruptedIOException 且**不是** SocketException（编译期不可 instanceof，用 Class.isInstance 断言；同理 SE 不是 STE）。
- bytesTransferred 契约：默认 0、可写、可为负、public 非 final（继承自 InterruptedIOException）、序列化存活。
- 构造器仍只有 ()/(String)（JDK-27 亦然）；toString 精确形式。
- serialVersionUID=-8846654841826352300L（双 JDK 一致）。
- 真实触发（回环）：accept SoTimeout(200)→"Accept timed out"（超时后可再 accept）；read SoTimeout(150)→"Read timed out"（socket 仍连接可再读）。
