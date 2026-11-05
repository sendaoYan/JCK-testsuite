# javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder — 设计文档

## 一、API 功能
`W3CEndpointReferenceBuilder`（public final，公共无参构造器）以流式 API 构建 W3CEndpointReference：10 个链方法 address/interfaceName/serviceName/endpointName/wsdlDocumentLocation/referenceParameter/metadata/element/attribute（均返回 this）+ build()。约束：address 必填（可空串）；endpointName 须在 serviceName 之后；设 serviceName 后 build 会真取 wsdlDocumentLocation。

## 二、使用方式
`new W3CEndpointReferenceBuilder().address(u).serviceName(qn).endpointName(pn).wsdlDocumentLocation(w).build()`；referenceParameter/metadata/element 接 org.w3c.dom.Element；attribute(QName,String) 写扩展属性。测试覆盖类面（10 方法/构造器/返回类型）、全链身份、地址形态、负例精确消息、本地 WSDL 支撑链。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder
- JSR 224 §8.2；W3C WS-Addressing 1.0 Metadata
- OpenJDK 8 内置 RI
- 本仓库测试：JDK-8/API/javax/xml/ws/wsaddressing/W3CEndpointReferenceBuilder/W3CEndpointReferenceBuilderTest.java

## 四、测试用例设计 + 易错规范细节
- 负例表（实测精确消息）：空 build / address(null) / serviceName 无地址 → ISE "Address in an EPR cannot be null, when serviceName or portName is null"；endpointName 先于 serviceName → ISE "The W3CEndpointReferenceBuilder's serviceName must be set before setting the endpointName: {urn:t}p"（在 setter 时即抛，非 build 时）；referenceParameter/metadata/element/attribute 值 null → IAE，消息分别为 "The referenceParameter cannot be null."/"The metadataElement cannot be null."/"The extension element cannot be null."/"The extension attribute name or value cannot be null."。
- 结构：address("") 合法（输出空 Address 元素）；后设 address 覆盖前者；多个 referenceParameter 全保留；attribute 落根元素属性（ns 前缀形式）；element 落 Metadata 之后扩展位；build 可重复（builder 复用）。
- WSDL 链：wsdlDocumentLocation + serviceName 会真取 WSDL——测试只用回环 HttpServer 提供的文档（绝不用外网 URL）；serviceName 与 WSDL 不符 → ISE "Error in parsing WSDL"。
- 易错点：流式身份断言中不能对已设 serviceName 的 builder 塞假 wsdlDocumentLocation 再 build（会真连网失败）。
