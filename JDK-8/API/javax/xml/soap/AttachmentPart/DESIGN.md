# AttachmentPart — 设计案

## 1. API 功能
SOAP 消息附件（abstract）：内容面 setContent(Object,String)/setRawContent(InputStream,String)/setRawContentBytes/setBase64Content/setDataHandler + getContent/getRawContent/getRawContentBytes/getBase64Content/getDataHandler/getSize/clearContent；元数据面 setContentId/setContentType/setContentLocation + 完整 MIME 头家族。

## 2. 使用方式
`ap = msg.createAttachmentPart(); ap.setContent("data","text/plain"); ap.setContentId("id"); msg.addAttachmentPart(ap); msg.saveChanges();` 读回经 `getAttachments()`/`getAttachments(hdr)`；MIME 往返需把含 boundary 的 Content-Type 传入 createMessage。getContent 返回类型跟随 setContent 的对象类型（String/byte[]/DataHandler 源）。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：内容基线（默认 null/size 0、String/byte[]/DataHandler 三源回读、size 与字节长）、三 id/type/location 回读 + setContentId 背书 Content-Id 头、MIME 头全家族、raw/base64 编解码对偶（setRawContentBytes("ABC")/setRawContent("XYZ")/getBase64Content=="V1hZWg=="/setBase64Content("WFhY")→"XXX"）、clearContent（getContent/getRawContent(Bytes) 抛 SOAPException、getSize 归 0、**头保留**、内容可重设）、消息侧全序（add/count/iterate/filter/remove/removeAll + 脱离消息仍可读）、含附件 MIME 往返（multipart 头 + 内容/id/soap 保真）。断言 89 条（旧 29）。
- 坑1：clearContent 后 getContent 抛 SOAPException "No data handler/content associated with this attachment"，但 **getSize() 返回 0 不抛**；MIME 头不被清除。
- 坑2：base64 通道是编码对偶：getBase64Content 给出内容的 base64 文本；setBase64Content 吃 base64 文本落原文。
- 坑3：setSize 不存在——getSize 反映当前内容字节数。
- 坑4：往返读回必须传原 Content-Type（multipart boundary），否则附件数为 0。
- 坑5：RI 在抛 SOAPException 前会向 java.util.logging 打 SEVERE 日志（stdout/stderr 可见），无害、不断言日志。
