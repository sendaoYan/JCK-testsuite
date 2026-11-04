# javax.print.event.PrintEvent 黑盒行为测试设计

## 1. API 功能
PrintEvent extends EventObject；构造器 (Object source)、toString。

## 2. Java 用户如何使用
打印事件基类。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/event/PrintEvent.java。

## 4. 测试用例设计 + 自动化实现
黑盒：getSource、toString 精确。白盒：public、extends EventObject。

