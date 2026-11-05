# SOAPMessage — 设计案

## 1. API 功能
SAAJ 根抽象类。持 SOAPPart（XML 载荷）+ 0..n AttachmentPart（MIME 附件）+ MimeHeaders（传输头）。生命周期方法 saveChanges/saveRequired/writeTo；属性面 setProperty/getProperty（WRITE_XML_DECLARATION/CHARACTER_SET_ENCODING）；附件管理 addAttachmentPart/getAttachments(hdr)/removeAttachments/removeAllAttachments/getAttachment(SOAPElement)。

## 2. 使用方式
核心闭环（本测试主通道）：构造→加业务载荷→`saveChanges()`→`writeTo(ByteArrayOutputStream)`→将原消息 Content-Type 复制到新 MimeHeaders→`MessageFactory.newInstance().createMessage(hdrs, bais)`→getElementsByTagNameNS/getTextContent 断言载荷保真。含附件时 Content-Type 变为 multipart（含 boundary），读回必须传该头，否则附件不解析。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：序列化往返（载荷元素计数+文本）、contentDescription set/get/null、属性面（两常量 set/get 回读、跨消息默认 null、未知属性行为）、saveChanges 幂等 + saveRequired 翻转、附件全序（create/add/count/iterate/filter/remove/removeAll/getAttachment-by-href）、含附件 MIME 往返。断言 74 条（旧 21）。
- 坑1：RI 对 `setProperty("bogus", v)` **不抛**、`getProperty("bogus")` 返回 null（比 javadoc "throws SOAPException if not recognized" 宽松——锁定 RI 行为）。
- 坑2：saveRequired 闩锁语义——saveChanges 后再改树仍为 false；全新未动树的消息 saveRequired()==true。
- 坑3：saveChanges+writeTo 后 Content-Type 为 "text/xml; charset=UTF-8"（1.1）；含附件时切 multipart。getMimeHeaders().getHeader 大小写不敏感。
- 坑4：getAttachment(SOAPElement) 按 href="cid:X" 与 Content-Id 头匹配：头值为尖括号形式 `<X>` 时命中；无 href 元素返回 null；setContentId("X")（无尖括号）配 "cid:X" 实测不命中。
- 坑5：getAttachments(空 MimeHeaders) 匹配全部附件；removeAttachments(空) 清空全部。
- 坑6：WRITE_XML_DECLARATION/CHARACTER_SET_ENCODING 常量串为 "javax.xml.soap.write-xml-declaration"/"javax.xml.soap.character-set-encoding"。
