# javax.xml.ws.handler.soap.SOAPMessageContext — 设计文档

## 一、API 功能
`SOAPMessageContext` 扩展 MessageContext，为 SOAPHandler 提供 4 个自有方法：`getMessage()`/`setMessage(SOAPMessage)`（取/换 SAAJ 消息）、`getHeaders(QName,JAXBContext,boolean)`（按头名 JAXB 解码所有同名头，allRoles 控制角色过滤）、`getRoles()`（链执行点生效的 SOAP 角色集）。

## 二、使用方式
在 SOAPHandler 回调内：getMessage().getSOAPHeader() 检视/写入头；setMessage 替换整条消息（如加签/变换后）；getHeaders 用 JAXB 直接解码自定义头 bean。测试用真实 SAAJ 消息写入头后经 getHeaders 解码回读，并在回环链路里验证头解码、角色集、入站信封命名空间与 setMessage 换出站消息。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.handler.soap.SOAPMessageContext
- JSR 224 §9.4.1
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.handler.SOAPMessageContextImpl
- 本仓库测试：JDK-8/API/javax/xml/ws/handler/soap/SOAPMessageContext/SOAPMessageContextTest.java

## 四、测试用例设计 + 易错规范细节
- 接口面：恰 4 个声明方法（getMessage/setMessage/getHeaders/getRoles）。
- 真实通道：出站 handler 内向 header 加 urn:h MyHeader(id=hid) → 服务端收到的信封含该头；getHeaders(QName,JAXBContext,false) 解码 bean 字段等于 hid；不存在的头 → 空数组（非 null）；getRoles 含 next；入站 envelope 命名空间为 SOAP 1.1；setMessage 换出站消息后 provider 收到换后内容。
- 易错点：(1) JAXB 头 bean 的字段同样要显式 @XmlElement(name,namespace)；(2) SAAJ 消息构造用 MessageFactory.newInstance()；(3) 所有消息断言在 handler 回调内完成。
