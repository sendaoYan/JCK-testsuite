# javax.xml.ws.handler.PortInfo — 设计文档

## 一、API 功能
`PortInfo` 携带端口三元组：`getServiceName()`（QName 服务名）、`getPortName()`（QName 端口名）、`getBindingID()`（String 绑定 URI，如 SOAP11HTTP_BINDING）。HandlerResolver.getHandlerChain(PortInfo) 依据它为不同端口返回不同处理器链。

## 二、使用方式
一般只读不实现：在自定义 HandlerResolver 内 switch 绑定 ID/端口名分流。测试自造实现验证三元组往返，并用真实 Service.addPort(SOAP11/SOAP12) + Dispatch 捕获运行时传入的真实 PortInfo。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.handler.PortInfo
- JSR 224 §9.2.1
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.handler.PortInfoImpl
- 本仓库测试：JDK-8/API/javax/xml/ws/handler/PortInfo/PortInfoTest.java

## 四、测试用例设计 + 易错规范细节
- 接口面：恰 3 个声明方法，无父接口（除隐式 Annotation 无——是普通接口）。
- 真实通道：SOAP11 端口捕获的 PortInfo 三元组全等（svc/{urn:t}p11/SOAP11HTTP_BINDING）；同一 Service 上 addPort SOAP12 后再建 Dispatch，PortInfo 切到 1.2 端口与绑定 ID，服务名不变。
- 易错点：端点按 SOAP 1.1 发布时，用 SOAP12HTTP_BINDING Dispatch 真正 invoke 会得 415 UnsupportedMedia——1.2 端口只验证 PortInfo 元数据，不发起调用（记录性约束）。
