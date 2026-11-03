# javax.print.DocFlavor 黑盒行为测试设计

## 1. API 功能
DocFlavor 描述打印数据格式（MIME + representation class）。构造器 (mimeType, className)、getMimeType/getMediaType/getMediaSubtype/getParameter/getRepresentationClassName/toString/equals/hashCode、hostEncoding。

## 2. Java 用户如何使用
new DocFlavor("text/plain; charset=utf-8","java.io.Reader") 描述文档；equals 比较规范化后的 MIME+class。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/DocFlavor.java。

## 4. 测试用例设计 + 自动化实现
黑盒：MIME 规范化、getParameter、toString 精确值、equals/hashCode、null/非法 MIME 异常。白盒：public、Serializable+Cloneable、方法形态。

