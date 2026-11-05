# ProtocolException 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.ProtocolException 是协议级异常（JSR 224 §9.3.1）：直接继承 WebServiceException，表示 SOAP/HTTP 协议处理失败（消息处理 handler 抛出时触发故障消息流）。子类：ActionNotSupportedException、HTTPException（http 包）、SOAPFaultException（soap 包）。

## 2. 使用方式
典型路径：`LogicalHandler.handleMessage` 中遇协议违约 `throw new ProtocolException("must-understand")` → 容器转为 fault。本测试：四构造器矩阵 + 双通道捕获（WebServiceException/RuntimeException）+ handler 抛出透传 + 序列化复活 + suppressed/fillInStackTrace。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/ProtocolException.html）、JSR 224 (JAX-WS 2.2) §9.3.1、本仓库 ProtocolExceptionTest。

## 4. 测试用例设计 + 易错规范细节
- 继承链：extends javax.xml.ws.WebServiceException（父测试见 WebServiceExceptionTest）→ RuntimeException；恰 4 public 构造器与父类同构。
- 构造矩阵：()/(String)/(String,Throwable)/(Throwable)——message/cause/toString/getLocalizedMessage 全断言；(Throwable) 派生 message 含 cause.toString()。
- 捕获通道：同时可作为 WebServiceException 与 RuntimeException 捕获（同一实例引用）。
- **真实透传场景（P0）**：LogicalHandler.handleMessage 抛 ProtocolException("must-understand") → 调用方同消息收到（容器不吞不换型）。
- 序列化 round-trip：writeObject→readObject instanceof ProtocolException + message 复活。
- Throwable 新面：addSuppressed/getSuppressed 引用保留；fillInStackTrace()==this。
- 混合族谱链：WebServiceException("l3", ProtocolException("l2", cause)) 三层遍历。
