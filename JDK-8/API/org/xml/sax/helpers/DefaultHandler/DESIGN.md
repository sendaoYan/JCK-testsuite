# DefaultHandler — 设计案

## 1. API 功能
SAX2 四合一空基类：EntityResolver+DTDHandler+ContentHandler+ErrorHandler。

## 2. Java 用户如何使用
匿名子类选覆写所需回调。

## 3. 开源代码实际用法调研
SAX 2.0.2 helpers javadoc；JAXP SAXParser.parse(InputStream,Handler) 参数类型。

## 4. 测试用例设计 + 自动化实现
黑盒：四接口实现锁定；resolveEntity 默认 null；warning/error 无操作而 fatalError 重抛包装异常；JAXP parse(InputStream,DefaultHandler) 带 DTD/记号/NDATA 文档静默通过；全 ContentHandler 回调集无操作分发。
