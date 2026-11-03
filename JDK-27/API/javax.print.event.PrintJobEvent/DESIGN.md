# javax.print.event.PrintJobEvent 黑盒行为测试设计

## 1. API 功能
PrintJobEvent extends PrintEvent；构造器 (DocPrintJob, int reason)、getPrintEventType/getPrintJob、六种事件类型常量。

## 2. Java 用户如何使用
作业状态变化时由实现方派发。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/event/PrintJobEvent.java。

## 4. 测试用例设计 + 自动化实现
黑盒：getPrintJob/getPrintEventType/常量精确值。白盒：public、extends PrintEvent。

