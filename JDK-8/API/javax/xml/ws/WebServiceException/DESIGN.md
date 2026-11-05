# WebServiceException 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.WebServiceException 是 JAX-WS 核心异常（JSR 224 §7.11.1）：所有 Web 服务相关异常的基类（子类 ProtocolException、ActionNotSupportedException 及各 spi/soap 包异常），表示非业务 fault 的框架层失败。四个构造器与 RuntimeException 同构。

## 2. 使用方式
典型路径：工厂/create 发现失败时抛出（如 Service.create(url,null) "null is not a valid service"、未发布 getEndpointReference "Endpoint is not published yet"）。本测试：四构造器全矩阵 message/cause 断言 + 继承链 + 序列化面。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/WebServiceException.html）、JSR 224 (JAX-WS 2.2)、本仓库 WebServiceExceptionTest。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵（P0）：()→message null/cause null；(String)→message 回读 cause null（空串保留非 null、显式 null message 为 null）；(String,Throwable)→双回读（null message+cause、message+null cause 变体）；(Throwable)→cause 引用同、message==cause.toString()（含异常类名与 "root"）。
- initCause 二次设置：构造已带 cause 后 initCause → IllegalStateException。
- 嵌套链：(Throwable) 套 (String,Throwable) 两层遍历直达根因。
- 继承链：extends RuntimeException → Exception → Throwable；非 Error；恰 4 public 构造器。
- 序列化：Serializable（异常基类）；ObjectStreamClass 描述存在；stack trace set/get round-trip。
- 族谱：ProtocolException 直接子类（后续 soap/http/spi 异常族以本类为根）。
