# javax.print.attribute.standard.Media 黑盒行为测试设计

## 1. API 功能
Media 抽象类 extends EnumSyntax implements DocAttribute/PrintRequestAttribute/PrintJobAttribute；protected Media(int) 构造器。子类 MediaName/MediaSizeName/MediaTray 提供预定义常量。

## 2. Java 用户如何使用
引用 MediaName.ISO_A4_WHITE 等常量；getCategory()==Media.class、getName()=="media"。继承 EnumSyntax 的值/身份语义。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/standard/Media.java。

## 4. 测试用例设计 + 自动化实现
黑盒：通过 MediaName/MediaSizeName 常量验证 category/name/value/EnumSyntax 身份语义。白盒：public abstract、extends EnumSyntax、implements 三接口。
