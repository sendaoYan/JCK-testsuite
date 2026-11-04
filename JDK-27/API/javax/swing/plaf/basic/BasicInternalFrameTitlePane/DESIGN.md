# javax.swing.plaf.basic.BasicInternalFrameTitlePane

## 1. API 功能

内部框架标题栏组件，承载标题、图标与最大化/最小化/关闭按钮。

## 2. Java 用户如何使用

由外观 installUI 时实例化，作为内部实现细节使用。

## 3. 开源代码实际用法调研

以 OpenJDK 源码（JDK-27 桌面模块 java.desktop）为准：

- 定义位置：`src/java.desktop/share/classes/javax/swing/plaf/basic/BasicInternalFrameTitlePane.java:59`
- 实际引用位置：`src/java.desktop/share/classes/javax/swing/plaf/basic/BasicInternalFrameTitlePane.java:190`

## 4. 测试用例设计 + 自动化实现

- 白盒（testWhiteBox）：通过 Class.forName 反射校验类的可加载性、修饰符、父类与实现的接口；
- 黑盒（testBlackBox）：在 EDT 上 实例化（或经工厂/反射获取）并组合调用关键方法，断言返回值与副作用；
- 自动化：采用 jtreg 单文件测试，headful 用例在事件分发线程（EDT）上运行，结束后释放组件。
