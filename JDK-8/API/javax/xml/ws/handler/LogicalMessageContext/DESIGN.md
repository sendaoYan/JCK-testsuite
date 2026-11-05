# javax.xml.ws.handler.LogicalMessageContext — 设计文档

## 一、API 功能
`LogicalMessageContext` 扩展 `MessageContext`，新增唯一方法 `getMessage()` 返回 `LogicalMessage`——对消息载荷的代理（4 个方法：getPayload(Source)/getPayload(JAXBContext)、setPayload(Source)/setPayload(Object,JAXBContext)），可在 LogicalHandler 中检视与改写 XML 载荷而不触及协议信封。

## 二、使用方式
在 LogicalHandler.handleMessage 中 `ctx.getMessage()` 取 LogicalMessage：`getPayload()` 读（Source 形式或 JAXB 反序列化），`setPayload()` 替换（Source 或 JAXB bean + JAXBContext）。改写入站消息会改变客户端收到的响应。测试自造实现验证 4 方法面，并在回环通道内于入站分支改写载荷观察客户端所见。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.handler.LogicalMessageContext、LogicalMessage
- JSR 224 §9.3.1
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.handler.LogicalMessageContextImpl
- 本仓库测试：JDK-8/API/javax/xml/ws/handler/LogicalMessageContext/LogicalMessageContextTest.java

## 四、测试用例设计 + 易错规范细节
- 接口面：恰 1 个声明方法 getMessage() 返回 LogicalMessage；父接口 MessageContext。
- LogicalMessage 面：4 个载荷方法签名精确（Source 族 + JAXB 族）。
- 真实通道：出站 setPayload(Source) → 服务端 provider 收到改写文本；入站 getPayload(JAXBContext) → bean 值为 provider 返回值；入站 setPayload(bean) 后在 handler 内 getPayload(jc) 读回改写值。
- 易错点：(1) JAXB bean 字段必须显式 @XmlElement(name=..., namespace=...)，否则 unmarshal 得 null（@XmlRootElement(namespace=...) 不下传到字段）；(2) invoke 返回后再读 payload 会抛 "Failed to copy a message"（流已耗尽）——setPayload/getPayload 断言必须放在 handler 回调内；(3) 上下文对象在两个方向被复用。
