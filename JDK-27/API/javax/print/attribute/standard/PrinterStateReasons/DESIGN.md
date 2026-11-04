# javax.print.attribute.standard.PrinterStateReasons 黑盒行为测试设计

## 1. API 功能
PrinterStateReasons extends HashMap<PrinterStateReason,Severity> implements PrintServiceAttribute；printerStateReasonSet(Severity)。

## 2. Java 用户如何使用
new PrinterStateReasons() 后 put(reason, severity)。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/standard/PrinterStateReasons.java。

## 4. 测试用例设计 + 自动化实现
黑盒：put/get、printerStateReasonSet 按 Severity 过滤、getName/getCategory。白盒：public final、extends HashMap、implements PrintServiceAttribute。

