# javax.print.DocFlavor.URL 黑盒行为测试设计

## 1. API 功能
DocFlavor.URL 嵌套常量族，representation class = java.net.URL，预定义 MIME 常量。

## 2. Java 用户如何使用
引用 DocFlavor.URL.PDF 等静态常量表示文档格式。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/DocFlavor.java。

## 4. 测试用例设计 + 自动化实现
黑盒：常量非空、representation class 精确、样例 MIME 精确、equals/hashCode、常量互异。白盒：public、extends DocFlavor。

