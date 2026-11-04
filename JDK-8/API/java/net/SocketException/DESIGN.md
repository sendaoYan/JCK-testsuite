# java.net.SocketException 测试设计案（JDK-8）

> 批次 F 深化：构造器矩阵、toString 精确形式、serialVersionUID、cause/suppressed 契约、序列化、跨线程传播、回环真实触发。

## 1. API 功能

- extends IOException；构造器仅 `()` 与 `(String)`（JDK 20+ 才有 (String,Throwable)/(Throwable)）。
- 所有套接字操作的通用失败异常。

## 2. 测试用例设计（SocketExceptionTest.java，65 断言）

- 构造器：() 消息 null、(String) 精确、(null) 保持 null、空串保留（toString "…: "）、非 ASCII 保留。
- 白盒：public 非抽象、superclass==IOException、serialVersionUID==-5935874303556886934L（双 JDK 一致）、**恰好 2 个公共构造器**、ctor(Throwable) 不存在。
- toString："java.net.SocketException: msg" / 无消息裸类名。
- cause：initCause 一次性（二次→ISE）、自因→IAE "Self-causation not permitted"、fresh+initCause(null) 合法。
- suppressed：初始 0、顺序保持、防御拷贝、自抑制→IAE；fillInStackTrace 返回 this。
- 序列化：message/cause/suppressed 全存活；null 消息存活。
- 跨线程：daemon 线程抛出后被捕获，类型/消息保持。
- 真实触发（回环）：closed socket `getInputStream/getSoTimeout`→"Socket is closed"。

## JDK-8 vs JDK-27 差异

- JDK-27（63 断言）：4 构造器；(Throwable) 消息==cause.toString()；其余契约一致。
