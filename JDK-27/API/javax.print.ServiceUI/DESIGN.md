# javax.print.ServiceUI 黑盒行为测试设计

## 1. API 功能
ServiceUI 提供 printDialog 静态方法弹出打印对话框（headful，本测试白盒不调用）。

## 2. Java 用户如何使用
ServiceUI.printDialog(...) 选择打印服务。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/ServiceUI.java。

## 4. 测试用例设计 + 自动化实现
白盒：public 类、printDialog 方法形态（不弹 UI）。

