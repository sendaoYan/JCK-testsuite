# javax.xml.ws.http.HTTPBinding — 设计文档

## 一、API 功能
`HTTPBinding` 扩展 `Binding`，是 XML/HTTP（非 SOAP）绑定的标记接口：自身零方法，唯一成员是常量 `HTTP_BINDING = "http://www.w3.org/2004/08/wsdl/http"`（2004/08 WSDL 2.0 期 HTTP 绑定 URI）。绑定的三方法（getBindingID/getHandlerChain/setHandlerChain）全部继承。

## 二、使用方式
`Service.create(...)` 后 `addPort(portName, HTTPBinding.HTTP_BINDING, url)`，再 `createDispatch(portName, Source.class, PAYLOAD)`：线上传输裸 XML 载荷（无 SOAP 信封），Content-Type 为 application/xml。测试用回环 HttpServer 验证服务端收到未包装 XML、响应裸传、绑定对象类型与 ID。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.http.HTTPBinding
- JSR 224 §11 XML/HTTP Binding
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.binding.HTTPBindingImpl
- 本仓库测试：JDK-8/API/javax/xml/ws/http/HTTPBinding/HTTPBindingTest.java

## 四、测试用例设计 + 易错规范细节
- 常量：恰 1 个声明字段；public static final String；与 4 个 SOAP 绑定 ID（SOAP11/SOAP12/两个 MTOM 变体）全部互异；前缀为 2004/08 wsdl 命名空间。
- 接口面：0 自有方法；继承 Binding 三方法（getHandlerChain 返回 List、setHandlerChain void、getBindingID String）。
- 真实通道：Dispatch.getBinding() instanceof HTTPBinding 且 ID 全等；服务端读到 `<q xmlns="urn:x">payload</q>` 原文且不含 "Envelope"；handler 链可 set/get 且装配后仍能正常裸传；响应上下文 HTTP_RESPONSE_CODE==200；同 Service 可再 addPort 第二个同绑定端口。
- 易错点：回环服务器需手动设置响应 Content-Type（application/xml），否则客户端解码行为依实现而定。
