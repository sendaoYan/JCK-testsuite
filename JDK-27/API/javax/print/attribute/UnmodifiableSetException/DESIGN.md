# javax.print.attribute.UnmodifiableSetException 黑盒行为测试设计

## 1. API 功能
UnmodifiableSetException 运行时异常，构造器 ()/(String)。

## 2. Java 用户如何使用
修改 unmodifiable 视图时抛出。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/UnmodifiableSetException.java。

## 4. 测试用例设计 + 自动化实现
黑盒：两个构造器、message。白盒：public、extends RuntimeException。

