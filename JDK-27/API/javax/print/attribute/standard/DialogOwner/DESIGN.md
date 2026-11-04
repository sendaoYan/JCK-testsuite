# javax.print.attribute.standard.DialogOwner 黑盒行为测试设计

## 1. API 功能
DialogOwner 指定打印对话框所有者窗口；构造器 ()/(Window)、getOwner/getCategory/getName。

## 2. Java 用户如何使用
new DialogOwner(window) 关联对话框父窗口；无参构造 getOwner 为 null。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/standard/DialogOwner.java。

## 4. 测试用例设计 + 自动化实现
黑盒：无参构造 getOwner==null、getName/getCategory（headless 不创建 Window）。白盒：public final、implements PrintRequestAttribute。

