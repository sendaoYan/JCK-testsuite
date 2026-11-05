# DTDHandler — 设计案

## 1. API 功能
DTD 记号与未解析实体回调：notationDecl(publicId,systemId)/unparsedEntityDecl(name,pid,sid,notationName)。

## 2. Java 用户如何使用
解析 DTD 时登记外部资源映射（如发 email 的 xml-stylesheet）。

## 3. 开源代码实际用法调研
JDK test/jdk/org/xml/sax；SAX 2.0.2 javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：带 <!NOTATION> 与 NDATA 实体的 DTD 真解析断言两回调实参；接口仅 2 方法锁定；DefaultHandler 空实现可用。
