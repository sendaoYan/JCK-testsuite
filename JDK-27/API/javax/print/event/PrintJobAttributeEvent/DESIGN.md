# javax.print.event.PrintJobAttributeEvent 黑盒行为测试设计

## 1. API 功能
PrintJobAttributeEvent extends PrintEvent；构造器 (DocPrintJob, PrintJobAttributeSet)、getPrintJob/getAttributes（不可变视图）。

## 2. Java 用户如何使用
作业属性变化事件。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/event/PrintJobAttributeEvent.java。

## 4. 测试用例设计 + 自动化实现
黑盒：getPrintJob、getAttributes 内容、不可变。白盒：public、extends PrintEvent。

