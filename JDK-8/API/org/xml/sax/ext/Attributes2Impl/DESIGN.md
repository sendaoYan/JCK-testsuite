# Attributes2Impl — 设计案

## 1. API 功能
Attributes2 可变实现，继承 AttributesImpl：新增 isSpecified/isDeclared/setSpecified/setDeclared。

## 2. Java 用户如何使用
构造/修改带 SAX2 标志位的属性表。

## 3. 开源代码实际用法调研
SAX 2.0.2 extensions javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：新条目默认 spec=T/decl=F；两 setter 往返；拷贝构造保标志位；removeAttribute 清空；越界 setSpecified → ArrayIndexOutOfBoundsException（注意与 AttributesImpl 越界行为族对照）。
