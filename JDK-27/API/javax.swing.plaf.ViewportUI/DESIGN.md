# javax.swing.plaf.ViewportUI

## 1. API 功能

Swing 外观（Look & Feel）委托抽象基类，为具体外观实现定义组件级绘制与行为契约。

## 2. Java 用户如何使用

通过组件的 getUI() 获取，或由具体外观的 createUI(Component) 工厂方法创建并 installUI/uninstallUI。

## 3. 开源代码实际用法调研

以 OpenJDK 源码（JDK-27 桌面模块 java.desktop）为准：

- 定义位置：`src/java.desktop/share/classes/javax/swing/plaf/ViewportUI.java:34`
- 实际引用位置：`src/java.desktop/share/classes/javax/swing/plaf/ViewportUI.java:38`

## 4. 测试用例设计 + 自动化实现

- 白盒（testWhiteBox）：通过 Class.forName 反射校验类的可加载性、修饰符、父类与实现的接口；
- 黑盒（testBlackBox）：在 EDT 上 实例化（或经工厂/反射获取）并组合调用关键方法，断言返回值与副作用；
- 自动化：采用 jtreg 单文件测试，headful 用例在事件分发线程（EDT）上运行，结束后释放组件。
