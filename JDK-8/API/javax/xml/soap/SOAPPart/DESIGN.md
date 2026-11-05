# SOAPPart — 设计案

## 1. API 功能
SOAP 消息的 XML 部分（abstract，同时实现 org.w3c.dom.Document 与 javax.xml.soap.Node）。持有 envelope 树；自带独立 MIME 头（Content-Id/Content-Location 等）；`getContent()/setContent(Source)` 提供与 javax.xml.transform 的桥（RI 实测 getContent 返回 DOMSource，接受 StreamSource 换入整棵树）。

## 2. 使用方式
`msg.getSOAPPart().getEnvelope()` 取树根；`setContent(new StreamSource(reader))` 整树替换后 getEnvelope() 得到新树；MIME 头族与 AttachmentPart 同构（addMimeHeader/setMimeHeader/getMimeHeader/remove*/getAll*/getMatching*/getNonMatching*）；DOM 视角直接当 Document 用（getElementsByTagNameNS 等）。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：getEnvelope 身份与 ns、getContent instanceof Source（精确 DOMSource）、setContent(StreamSource) 回读（envelope ns + 导入元素 + 文本）、MIME 头全家族（多值保序/大小写不敏感/setMimeHeader 替换/matching 与 non-matching 空数组语义）、setContentId/setContentLocation 与头联动、DOM 视角（getElementsByTagNameNS/getElementsByTagName 限定名语义/documentElement==envelope）。断言 67 条（旧 20）。
- 坑1：默认 SOAPPart 可能带初始 MIME 头——精确计数前先 removeAllMimeHeaders()。
- 坑2：DOM getElementsByTagName 匹配**限定标签名**（"p:pay"/"SOAP-ENV:Body"），裸 local 名查不到；getElementsByTagNameNS 才按 ns+local。
- 坑3：getMimeHeader 查找大小写不敏感（"Content-Id"/"content-id"/"CONTENT-ID" 等价）。
- 坑4：getMatchingMimeHeaders(空数组)==0、getNonMatchingMimeHeaders(空数组)==全部（与 null 同义）。
- 坑5：DOM 树查询前先 getEnvelope()/getSOAPBody() 实现树（懒构建路径下直接查 document 计数可能为 0，实测以先实现为准）。
