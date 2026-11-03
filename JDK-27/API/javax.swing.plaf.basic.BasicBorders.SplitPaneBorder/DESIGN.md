# javax.swing.plaf.basic.BasicBorders.SplitPaneBorder

## 1. API 功能

外观默认边框实现类，提供组件边框的绘制与内边距计算。

## 2. Java 用户如何使用

由外观工厂（如 BasicBorders.getXxxBorder / BorderFactory）构造后 setBorder 到组件。

## 3. 开源代码实际用法调研

以 OpenJDK 源码（JDK-27 桌面模块 java.desktop）为准：

- 定义位置：`src/java.desktop/share/classes/javax/swing/plaf/basic/BasicBorders.java:659`
- 实际引用位置：`src/java.desktop/share/classes/javax/swing/plaf/basic/BasicBorders.java:127`

## 4. 测试用例设计 + 自动化实现

- 白盒（testWhiteBox）：通过 Class.forName 反射校验类的可加载性、修饰符、父类与实现的接口；
- 黑盒（testBlackBox）：直接 实例化（或经工厂/反射获取）并组合调用关键方法，断言返回值与副作用；
- 自动化：采用 jtreg 单文件测试，纯数据/接口用例直接在主线程在事件分发线程（EDT）上运行，结束后释放组件。
