# javax.print.PrintServiceLookup 黑盒行为测试设计

## 1. API 功能
PrintServiceLookup 抽象类，静态 lookupPrintServices/lookupMultiDocPrintServices/lookupDefaultPrintService + registerServiceProvider/registerService，抽象 getPrintServices/getMultiDocPrintServices/getDefaultPrintService。

## 2. Java 用户如何使用
通过静态 lookup 发现打印服务（本测试白盒，不真查打印机）。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/PrintServiceLookup.java。

## 4. 测试用例设计 + 自动化实现
白盒：public abstract、静态/抽象方法形态（不触发打印机枚举）。

