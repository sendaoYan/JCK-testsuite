# javax.xml.transform.stream(子包目录级)— 设计案

## 1. API 功能
TrAX 的流桥:StreamSource(InputStream/Reader/File/systemId 六构造家族)、StreamResult(OutputStream/Writer/File/systemId 七构造家族)。常量 FEATURE 各一份。JDK-27 的 StreamSource 覆写 isEmpty()。

## 2. Java 用户如何使用
最常用 IO 形态:`new StreamSource(new StringReader(xml))` 入、`new StreamResult(sw)` 出;File 构造自动派生 file: URL 作 systemId。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 基线用例;XSLTC serializer 按 Writer/OutputStream 分派,默认 encoding UTF-8。

## 4. 测试用例设计 + 易错细节(目录级汇总)
两个类各自测试见子目录。子包级闭环:File→transform→精确输出(临时文件 finally 删);OutputStream 路径按 UTF-8 解码逐字符相等;Writer 路径精确;(File) 与 setSystemId(File) 均 endsWith(".xml") 的 file URL;systemId 与 sink 互相独立。
易错:不依赖默认字符集(构造/断言显式 UTF-8);不断言平台换行(replace "\r");JDK-27 StreamSource.isEmpty() 无流无 systemId 时 true、setSystemId 后 false。
