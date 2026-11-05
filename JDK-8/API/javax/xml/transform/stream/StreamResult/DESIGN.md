# javax.xml.transform.stream.StreamResult — 设计案

## 1. API 功能
以流为输出的 Result 实现。常量 `FEATURE="http://javax.xml.transform.stream.StreamResult/feature"`。构造器:()、(OutputStream)、(OutputStream,String)、(Writer)、(Writer,String)、(String systemId)、(File);方法 get/setOutputStream、get/setWriter、get/setSystemId(File 或 String)。

## 2. Java 用户如何使用
`new StreamResult(new StringWriter())` 收集文本;`new StreamResult(file)` 直写文件;`new StreamResult(bos, "sid")` 流+基准 URI。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 最常用输出形态;XSLTC serializer 按 writer/OutputStream 分派。

## 4. 测试用例设计 + 易错细节
黑盒:构造矩阵 round-trip——(OutputStream)/(Writer) 均有对应 (流,String) 双参构造(与 StreamResult 的 sink+systemId 家族区分:StreamResult 有,StreamSource 同样有);(systemId) 构造 sink 皆 null;(File) 与 setSystemId(File) 派生 file URL;setWriter/setOutputStream 互换后 getter 各自最新;FEATURE 精确值。
真实闭环:OutputStream 路径——恒等转换后 new String(bos.toByteArray(), "UTF-8") 与 DOC 逐字符相等(显式 UTF-8,防平台默认);Writer 路径同样精确。
易错:(1) OutputStream 输出必须按样式表 encoding(默认 UTF-8)解码断言;(2) 换行断言前 replace("\r","") 抹平;(3) File 临时输出要 finally delete。
