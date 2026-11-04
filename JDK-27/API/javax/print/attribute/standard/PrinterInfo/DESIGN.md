# javax.print.attribute.standard.PrinterInfo 黑盒行为测试设计

## 1. API 功能
TextSyntax 子类，构造器 PrinterInfo(String value, Locale locale)。getValue/getLocale/getName/getCategory/equals/hashCode/toString（toString 返回文本值，固定 Locale 下确定）。

## 2. Java 用户如何使用
new PrinterInfo("text", Locale.ROOT) 作为文本属性；null value 抛 NPE，null locale 回退 Locale.getDefault()。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/standard/PrinterInfo.java。

## 4. 测试用例设计 + 自动化实现
黑盒：value/locale/name/category/toString 精确值、equals 对 value 和 locale 敏感、hashCode、null 构造行为。白盒：public final、extends TextSyntax、implements 各接口。

