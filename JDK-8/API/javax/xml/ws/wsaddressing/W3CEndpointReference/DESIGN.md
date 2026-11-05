# javax.xml.ws.wsaddressing.W3CEndpointReference — 设计文档

## 一、API 功能
`W3CEndpointReference`（public final，扩展 EndpointReference）是 W3C WS-Addressing 1.0 EndpointReference 的 JAXB 绑定形式：公共构造器 `W3CEndpointReference(Source)`（解析 WSA 命名空间 XML）、protected 无参构造器（RI 用）、唯一公共自有方法 `writeTo(Result)`（规范序列化）+ protected static 常量 NS="http://www.w3.org/2005/08/addressing"。继承 readFrom/getPort/toString。

## 二、使用方式
从 Source 读入（或由 Endpoint.getEndpointReference/W3CEndpointReferenceBuilder 产出），writeTo 序列化、getPort(Class) 直接对 EPR 建代理（需 WSDL 元数据）。测试覆盖构造/序列化往返、toString 等价、负例精确异常、宽容根规范化、equals 身份、readFrom 族、活端点 EPR 元数据。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.wsaddressing.W3CEndpointReference；W3C WS-Addressing 1.0 Core
- JSR 224 §8 EndpointReference
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.wsaddressing.W3CEndpointReference
- 本仓库测试：JDK-8/API/javax/xml/ws/wsaddressing/W3CEndpointReference/W3CEndpointReferenceTest.java

## 四、测试用例设计 + 易错规范细节
- 负例表（实测）：ctor(null) → IAE("null")；坏 XML → WebServiceException "Error unmarshalling W3CEndpointReference"；writeTo(null) → WebServiceException "Error marshalling W3CEndpointReference."；readFrom(null) → IAE "source parameter must not be null"；getPort 无 WSDL 元数据 → WebServiceException "WSDL metadata is missing in EPR"。
- 宽容面（实测记录）：错根元素/错命名空间不抛异常，writeTo 输出被规范化为 wsa:EndpointReference（原命名空间被丢弃）。
- equals/hashCode 未覆写（身份语义）；toString 与 writeTo 内容一致。
- 活通道：localhost:0 发布后 getEndpointReference(W3CEndpointReference.class) → Address 含实际端口、EndpointName="p"、ServiceName svc、?wsdl；无类型重载同样返回 W3C 形式。
- 易错点：类内含私有静态 getW3CJaxbContext()，统计公共自有方法须按 public 过滤（共恰 1 个 writeTo）。
