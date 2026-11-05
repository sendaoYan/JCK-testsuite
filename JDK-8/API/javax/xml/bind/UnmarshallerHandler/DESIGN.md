# UnmarshallerHandler 设计案（JDK-8 契约测试）

## 1. API 功能
Unmarshaller 与 SAX 的桥：实现 org.xml.sax.ContentHandler 接收 SAX 事件，getResult() 在 parse 完成后返回 JAXB 对象。

## 2. 使用方式
XMLReader.setContentHandler(unmarshaller.getUnmarshallerHandler()) → reader.parse(source) → handler.getResult()。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/bind/)、JSR 222 (JAXB 2.2) 规范、OpenJDK 8u 内置 RI（com.sun.xml.internal.bind.v2）、本仓库 JAXBContextTest 及同包族测试。

## 4. 测试用例设计 + 易错规范细节
- 接口 = ContentHandler + 唯一声明方法 getResult。
- SAX 通道闭环：真实 XMLReader 驱动 parse，getResult() 得到 Person 且 name/age 全等。
- 每次 getUnmarshallerHandler() 返回**新** handler；getResult 可重复调用稳定。
- parse 前 getResult：null 或 IllegalStateException（实测宽松断言）。
