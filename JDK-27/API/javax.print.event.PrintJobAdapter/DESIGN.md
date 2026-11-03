# javax.print.event.PrintJobAdapter 黑盒行为测试设计

## 1. API 功能
PrintJobAdapter 抽象类实现 PrintJobListener，六个回调默认空实现。

## 2. Java 用户如何使用
继承并只覆写关心的回调。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/event/PrintJobAdapter.java。

## 4. 测试用例设计 + 自动化实现
黑盒：匿名子类调用全部空实现不抛异常。白盒：public abstract、implements PrintJobListener。

