# Attributes — 设计案

## 1. API 功能
SAX2 元素属性表：getLength/getURI/getLocalName/getQName/getType/getValue（索引、qName、uri+local 三通道）+ getIndex。

## 2. Java 用户如何使用
ContentHandler.startElement 第四参数；按命名空间或原始 qName 查值。

## 3. 开源代码实际用法调研
JDK test/jdk/org/xml/sax/ptests；SAX 2.0.2 javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：ns=T+pfx=F 时 xmlns 声明不出现在表中（len=2）；uri+local 命中；缺名 null/-1；越界 getValue/getType/getLocalName/getQName=null 而活表 getURI(9) 返回空串（非 null，Xerces 活表差异）；isDeclared/isSpecified 在 ext.Attributes2。
