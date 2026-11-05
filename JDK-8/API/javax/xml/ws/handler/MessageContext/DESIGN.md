# javax.xml.ws.handler.MessageContext — 设计文档

## 一、API 功能
`MessageContext` 扩展 `Map<String,Object>`，是处理器链中消息相关上下文的载体：18 个 String 常量键（MESSAGE_OUTBOUND_PROPERTY、WSDL_SERVICE/WSDL_PORT/WSDL_OPERATION/WSDL_INTERFACE、HTTP_REQUEST_HEADERS/HTTP_RESPONSE_HEADERS/HTTP_REQUEST_METHOD/HTTP_REQUEST_URL/HTTP_RESPONSE_CODE、SERVLET_REQUEST/RESPONSE/CONTEXT/SESSION（4 个）、QUERY_STRING、PATH_INFO、INBOUND/OUTBOUND_MESSAGE_ATTACHMENTS）+ setScope/getScope 两个自有方法（属性作用域 APPLICATION/HANDLER）。

## 二、使用方式
处理器回调里以 Map 方式读写属性；APPLICATION 域属性对服务端实现可见，HANDLER 域仅处理器可见。运行时在不同阶段填充标准键（出站含 WSDL_*、HTTP 请求侧信息；入站含响应码等）。测试覆盖常量全表、自造实现的 scope 语义、真实通道回调内快照键值。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.handler.MessageContext
- JSR 224 §9.4 Message Context
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.handler.MessageContextImpl
- 本仓库测试：JDK-8/API/javax/xml/ws/handler/MessageContext/MessageContextTest.java

## 四、测试用例设计 + 易错规范细节
- 常量：恰 18 个（javap 实测；易数错成 19），逐个精确全等 + 两两互异。
- 真实通道（回调内快照）：WSDL_SERVICE 等于服务名；无 WSDL 的 Dispatch 上 WSDL_PORT 为 null（只填 SERVICE）；HTTP_RESPONSE_CODE==200、HTTP_RESPONSE_HEADERS 非空；SERVLET_*/QUERY_STRING/PATH_INFO 均为 null。
- scope：getScope 不存在键 → IAE "Property X does not exist."；setScope(null,...) → IAE "Property null does not exist."；setScope(k,null) 容忍（实测）。
- 易错点：(1) 所有键值断言必须在 handleMessage 内快照——invoke 返回后上下文被清/复用（MESSAGE_OUTBOUND_PROPERTY 双向都读 false）；(2) INBOUND_MESSAGE_ATTACHMENTS 可能为 Map，断言放宽为 "null 或 Map"；(3) put 新键默认 APPLICATION 域。
