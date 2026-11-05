# javax.xml.transform.stream.StreamSource — 设计案

## 1. API 功能
以流为输入的 Source 实现。常量 `FEATURE="http://javax.xml.transform.stream.StreamSource/feature"`。6 个构造器:()、(InputStream)、(InputStream,String)、(Reader)、(Reader,String)、(String systemId)、(File);方法 get/setInputStream、get/setReader、get/setPublicId、get/setSystemId(File 或 String)。JDK-8 无 isEmpty()。

## 2. Java 用户如何使用
`new StreamSource(new StringReader(xml))` 或文件/URL systemId 直接交给 transform/newTemplates。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 最常用输入形态;File 构造派生 file: URL 作为 systemId。

## 4. 测试用例设计 + 易错细节
黑盒:全部 6 构造器 round-trip(InputStream 同引用+systemId、Reader 同引用+systemId、systemId-only 派生空流、publicId 独立);(File) 构造与 setSystemId(File) 都派生 .xml 结尾的 file URL;FEATURE 常量精确值。
真实闭环:临时文件写 DOC → StreamSource(File) 恒等转换输出精确(临时文件 finally 删除)。
易错:(1) setSystemId(File) 重载与 setSystemId(String) 重载派生规则一致(file.toURL);(2) reader 与 inputStream 同时只应设一个,后设者胜但另一者保留旧值(实现不清理);(3) 不依赖平台默认字符集——构造 Reader/写文件都显式 UTF-8。
