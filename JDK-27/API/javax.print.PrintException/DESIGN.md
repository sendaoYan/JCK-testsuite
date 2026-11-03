# javax.print.PrintException 黑盒行为测试设计

## 1. API 功能
PrintException 异常，构造器 ()/(String)/(Exception)/(String,Exception)。

## 2. Java 用户如何使用
打印失败时抛出，包装底层 cause。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/PrintException.java。

## 4. 测试用例设计 + 自动化实现
黑盒：四个构造器的 message/cause。白盒：public、extends Exception。

