# javax.xml.ws.spi.http.HttpExchange — 设计文档

## 一、API 功能
`HttpExchange`（public abstract，公共无参构造器）抽象一次 HTTP 请求/响应：恰 22 个声明方法全部抽象（实测 getRequestHeader 亦为抽象，无具体便捷方法），覆盖请求头（getRequestHeaders/getRequestHeader）、响应头（getResponseHeaders/addResponseHeader）、流（getRequestBody/getResponseBody）、连接信息（getRequestURI/getRequestMethod/getContextPath/getPathInfo/getQueryString/getProtocol/getScheme/getRemoteAddress/getLocalAddress/getHttpContext）、安全（getUserPrincipal/isUserInRole）、属性（getAttribute/getAttributeNames）、生命周期（setStatus/close）。3 个 String 常量属性键：REQUEST_CIPHER_SUITE、REQUEST_KEY_SIZE、REQUEST_X509CERTIFICATE。

## 二、使用方式
容器实现全部 22 方法后把实例交给 HttpHandler.handle(exchange)：运行时读请求流解码 SOAP、写状态码/响应头/响应流并 close。测试覆盖类面/常量全等、自造交换语义（首值/追加头/属性/流），并用"Endpoint.publish(spi 上下文) 捕获 RI 处理器 → 自造 exchange 驱动 handle()"的进程内闭环验证 200/SOAP 信封/Content-Type。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.spi.http.HttpExchange
- JSR 224 §13.3
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.transport.http.HttpAdapter.handle(SOAP/...)
- 本仓库测试：JDK-8/API/javax/xml/ws/spi/http/HttpExchange/HttpExchangeTest.java

## 四、测试用例设计 + 易错规范细节
- 常量精确值：javax.xml.ws.spi.http.request.cipher.suite / .request.key.size / .request.cert.X509Certificate，两两互异。
- 进程内闭环：MyContext.setHandler 捕获 RI 处理器 → handle(SimpleExchange 携 SOAP11 信封) → setStatus(200)、close、响应字节含 Envelope+载荷、Content-Type text/xml；坏请求体（非 XML）静默 stderr 后断言"异常或非 200"，绝不干净 200。
- 易错点：(1) getRequestHeader 是抽象方法（RI 未提供默认实现，javap 实测 22 全抽象）；(2) 坏体路径 RI 会向 stderr 打栈，须临时重定向 System.err 保持输出干净；(3) close() 声明 IOException，自造实现的 handle 需 throws。
