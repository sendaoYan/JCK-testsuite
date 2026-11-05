# MimeHeaders — 设计案

## 1. API 功能
传输头表（可实例化具体类）：addHeader（同名多值追加）/setHeader（同名替换）/getHeader（数组）/removeHeader/removeAllHeaders/getAllHeaders/getMatchingHeaders(names)/getNonMatchingHeaders(names)。SOAPMessage/SOAPPart/AttachmentPart 三处共用。

## 2. 使用方式
`mh.addHeader("Content-Type","text/xml")` 追加；读回时把消息的 Content-Type 复制进新 MimeHeaders 传给 createMessage 恢复 multipart boundary；getMatching/NonMatching 按（大小写不敏感的）名字集合切分视图。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：多值保序、setHeader 替换/不存在即建、大小写敏感性分区实测（getHeader/removeHeader/matching 不敏感，**setHeader 定位区分**）、matching 语义（数组/空数组/null）、null 名负例（IAE）、null 值行为、迭代（元素为 MimeHeader、name/value 回读、removeAllHeaders 后复用）。断言 36 条（旧 5）。
- 坑1：**addHeader(null, v)/setHeader(null, v) → IllegalArgumentException "Illegal MimeHeader name"**。
- 坑2：**addHeader(name, null) 不抛但头不入表**（getHeader 返回 null）。
- 坑3：setHeader 用不同大小写不替换旧头而是**新建平行头**（"x-lower" 与 "X-UPPER" 共存）；removeHeader/getHeader/matching 则大小写不敏感。
- 坑4：getMatchingHeaders(null) 返回空（RI 按 null==空数组处理），并非"全部"；getNonMatchingHeaders(null) 返回全部。
- 坑5：getAllHeaders 迭代元素是 javax.xml.soap.MimeHeader 实例。
