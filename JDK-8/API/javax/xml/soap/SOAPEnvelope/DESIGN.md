# SOAPEnvelope — 设计案

## 1. API 功能
SOAP 信封（extends SOAPElement）：树的根元素（ns 区分 1.1/1.2），持 getHeader()/getBody()/addHeader()/addBody() 与 createName(local)/createName(local,prefix,uri) 工厂。

## 2. 使用方式
`msg.getSOAPPart().getEnvelope()` 取得；addHeader/addBody 用于 detach 后重建（一个信封至多一个 header 一个 body）；createName 为本消息树造 Name（可直接喂 addChildElement/addAttribute）。本测试同时覆盖 1.1/1.2 两个工厂形态。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：结构（header/body 非 null + parentNode/parentElement 双面 == envelope）、addHeader/addBody 重复抛 SOAPException（消息含 "header"/"body"）、detach 后重建、createName 两形态回读 + equals/hashCode、1.1 vs 1.2 ns 全等矩阵（含与 SOAPConstants.URI_NS_* 对齐）。断言 56 条（旧 9）。
- 坑1：默认消息已带 header/body——直接 addHeader() 必抛 "Can't add a header when one is already present."；先 `getHeader().detachNode()` 再 addHeader。
- 坑2：envelope 自身元素名 prefix 为 "SOAP-ENV"（1.1）。
- 坑3：1.2 上重复 addHeader 同样拒绝；createName 语义两版本一致。
