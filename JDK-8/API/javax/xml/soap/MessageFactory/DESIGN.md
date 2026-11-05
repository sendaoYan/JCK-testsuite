# MessageFactory — 设计案

## 1. API 功能
SOAP 消息工厂（abstract）。`newInstance()` 按查找顺序定位 SAAJ 实现（JDK-8 内置 com.sun.xml.internal.messaging.saaj.soap.MessageFactoryImpl）；`newInstance(String protocol)` 按协议选择器创建：SOAP 1.1 / SOAP 1.2 / Dynamic / Dynamic Protocol。`createMessage()` 建空消息；`createMessage(MimeHeaders, InputStream)` 从字节流（多为 MIME 流）恢复消息。SAAJ 消息树（SOAPPart→Envelope→Header/Body）的唯一切入点。

## 2. 使用方式
- `MessageFactory.newInstance().createMessage()` → 默认 SOAP 1.1 消息（envelope ns http://schemas.xmlsoap.org/soap/envelope/）。
- `newInstance(SOAPConstants.SOAP_1_2_PROTOCOL)` → 1.2 消息（ns http://www.w3.org/2003/05/soap-envelope）。
- 写侧：`msg.getSOAPBody().addBodyElement(qname).addTextNode(v)` → `saveChanges()` → `writeTo(out)`。
- 读侧：把原消息的 Content-Type（含 multipart boundary）复制进 MimeHeaders 后 `createMessage(hdrs, bais)`（本测试闭环路径）。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：协议选择矩阵（默认/1.1/1.2/DEFAULT==1.1/Dynamic）、层级链（Part→Envelope→Header/Body 非 null + 父子关系）、流式恢复（正常往返、null 头、空流、坏流、垃圾流）、负例精确异常。断言 35 条（旧 4）。
- 坑1：DYNAMIC_SOAP_PROTOCOL 工厂可创建但 `createMessage()` 抛 **UnsupportedOperationException**（非 SOAPException）。
- 坑2：未知协议串 → SOAPException 且消息 "Unknown Protocol: <串> specified for creating MessageFactory"。
- 坑3：空流/坏流/垃圾流均为**懒解析**——createMessage 正常返回，首次 getSOAPPart().getEnvelope()/getSOAPBody() 才抛 SOAPException（"Unable to create envelope from given source"）。
- 坑4：createMessage(null, stream) 合法（等价空头）。
- 坑5：默认消息本身带 Header（getHeader() != null），env.addHeader() 立即抛 "Can't add a header when one is already present."。
