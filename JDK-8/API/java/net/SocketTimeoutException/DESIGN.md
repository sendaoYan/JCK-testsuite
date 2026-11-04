# java.net.SocketTimeoutException 测试设计案（JDK-8）

> 批次 F 深化：关键分叉（extends InterruptedIOException 而非 SocketException）、bytesTransferred 契约、双真实触发。

## 1. API 功能

- extends InterruptedIOException（→IOException）；构造器 `()`/`(String)`。
- 语义分叉：超时是"中断"而非"套接字错误"——与 SocketException 是 IOException 下两支互斥分支。

## 2. 测试用例设计（SocketTimeoutExceptionTest.java，60 断言）

- 分叉断言：`SocketException.class.isInstance(ste)`==false（instanceof 直接写会编译错——类型系统可证明不相关）；反向同理。
- bytesTransferred：默认 0、可写、可为负、public 非 final（继承自 InterruptedIOException）、序列化存活。
- 白盒：serialVersionUID==-8846654841826352300L（双 JDK 一致）、恰好 2 构造器、ctor(Throwable) 不存在。
- toString 精确形式；cause/suppressed/序列化/跨线程全套。
- 真实触发 1（回环）：ServerSocket SoTimeout(200) 后 accept→"Accept timed out"，超时后 server 仍可再 accept。
- 真实触发 2（回环）：连接对 + SoTimeout(150) 读→"Read timed out"，超时后 socket 仍连接、可再读。

## 易错规范细节（已锁入断言）

- catch (SocketException) **捕获不到** SocketTimeoutException；按超时处理需单独 catch 或按 IOException。
- bytesTransferred 由 InterruptedIO 类操作设置，accept 超时时保持 0。

## JDK-8 vs JDK-27 差异

- 无差异（57 断言，消息与契约双 JDK 一致）。
