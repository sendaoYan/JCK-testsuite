# javax.xml.transform.OutputKeys — 设计案

## 1. API 功能
输出属性键常量类:METHOD("method")、VERSION("version")、ENCODING("encoding")、OMIT_XML_DECLARATION("omit-xml-declaration")、STANDALONE("standalone")、CDATA_SECTION_ELEMENTS("cdata-section-elements")、INDENT("indent")、MEDIA_TYPE("media-type")、DOCTYPE_PUBLIC("doctype-public")、DOCTYPE_SYSTEM("doctype-system"),共 10 个。类为 public 非 final、构造器私有、不可实例化。

## 2. Java 用户如何使用
`t.setOutputProperty(OutputKeys.INDENT, "yes")` 或作为 setOutputProperties(Properties) 的键。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 的输出控制用例(缩进/编码/DOCTYPE/CDATA)全部经 OutputKeys 取键;XSLTC 内部以同名字符串消费。

## 4. 测试用例设计 + 易错细节
白盒:10 个常量精确字符串值逐一锁定(如 "omit-xml-declaration"、"" 空串不出现于键);类结构(public、非 final、private ctor);常量间两两互异且非空。
易错:(1) ENCODING 的值是属性"键"不是字符集名;(2) DOCTYPE_PUBLIC/DOCTYPE_SYSTEM 两键值最易记混——"doctype-public"/"doctype-system";(3) 层级上 OutputKeys 属于 javax.xml.transform 直包(无子包)。
