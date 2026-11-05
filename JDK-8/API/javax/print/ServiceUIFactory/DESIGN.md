# javax.print.ServiceUIFactory 黑盒行为测试设计

## 1. API 功能
抽象类：getUI(role, uiType)/getUIClassNamesForRole(role) + 常量 ABOUT=1/ADMIN=2/MAIN=3/RESERVED=99 + JCOMPONENT_UI/PANEL_UI/DIALOG_UI/JDIALOG_UI 字符串。

## 2. Java 用户如何使用
服务 UI 工厂。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
常量全等（4 int + 4 String 精确值）；RecFactory：ABOUT_UIROLE 命中返回、未知 role null、MAIN_UIROLE 类名数组 1 项、未知 role 空。

## 5. 负例与精确异常
无（接口无约束）。

## 6. 闭环与版本差异
无版本差异。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。
