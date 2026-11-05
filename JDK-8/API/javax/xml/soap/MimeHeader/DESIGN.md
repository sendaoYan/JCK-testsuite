# MimeHeader — 设计案

## 1. API 功能
单个 MIME 头值对象：getName()/getValue()，公开构造 `new MimeHeader(name, value)`。SAAJ 中少有的可直接实例化的值类型；MimeHeaders 迭代器的元素类型。

## 2. 使用方式
直建：`new MimeHeader("Content-Type","text/plain")`；或从 SOAPPart/AttachmentPart 的 getAllMimeHeaders() 迭代取得实例回读。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：公开构造回读、equals/hashCode 语义（**身份语义**——未覆写，同参两实例不等）、不同名/值不等、空串名值合法、null 名值实测接受、类形态（具体类非接口）、真实通道（SOAPPart 头表多值迭代、AttachmentPart 头表含 setContentId/setContentType 自动头、getMatching/NonMatching 过滤视角）。断言 26 条（旧 3）。
- 坑1：**未覆写 equals/hashCode**——同参两实例 equals==false，hashCode 不同；不要按值语义断言。
- 坑2：构造器接受 null 名/值（无校验），getName()==null 可读回。
- 坑3：AttachmentPart 上 setContentId/setContent/setContentType 自动产生 Content-Id/Content-Type 头，可在迭代中读到。
