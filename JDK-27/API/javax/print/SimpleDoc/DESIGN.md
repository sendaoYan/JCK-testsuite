# javax.print.SimpleDoc 黑盒行为测试设计

## 1. API 功能
SimpleDoc 是 Doc 的简单实现：getDocFlavor/getPrintData/getAttributes/getReaderForText/getStreamForBytes。构造器校验 printData 类型与 DocFlavor 匹配。

## 2. Java 用户如何使用
new SimpleDoc(data, flavor, attrs) 后交给 DocPrintJob.print。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/SimpleDoc.java。

## 4. 测试用例设计 + 自动化实现
黑盒：text/byte 往返 reader/stream、attributes 不可变、null/类型不匹配 IAE。白盒：public final、implements Doc。

