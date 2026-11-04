# javax.print.ServiceUIFactory 黑盒行为测试设计

## 1. API 功能
ServiceUIFactory 抽象类，UI 类型常量（JCOMPONENT_UI/PANEL_UI/DIALOG_UI/JDIALOG_UI）和 UI 角色常量（ABOUT/ADMIN/MAIN/RESERVED_UIROLE），抽象 getUI/getUIClassNamesForRole。

## 2. Java 用户如何使用
打印服务实现方继承并返回 UI；客户端按 role 查询。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/ServiceUIFactory.java。

## 4. 测试用例设计 + 自动化实现
黑盒：常量精确值。白盒：public abstract、抽象方法。

