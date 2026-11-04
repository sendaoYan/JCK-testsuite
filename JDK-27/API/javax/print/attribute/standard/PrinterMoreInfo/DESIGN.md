# javax.print.attribute.standard.PrinterMoreInfo 黑盒行为测试设计

## 1. API 功能
URISyntax 子类，构造器 PrinterMoreInfo(URI)。getURI/getName/getCategory/equals/hashCode/toString（toString 返回 URI 字符串）。

## 2. Java 用户如何使用
new PrinterMoreInfo(URI.create("ipp://...")) 作为 URI 属性；null 抛 NPE。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/standard/PrinterMoreInfo.java。

## 4. 测试用例设计 + 自动化实现
黑盒：URI 往返、name/category/toString 精确值、equals/hashCode、null NPE。白盒：public final、extends URISyntax、implements 各接口。

