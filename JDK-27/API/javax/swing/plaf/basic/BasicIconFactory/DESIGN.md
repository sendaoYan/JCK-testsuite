# javax.swing.plaf.basic.BasicIconFactory

## 1. API 功能

Swing Basic 外观的静态工具类，提供绘制、HTML 渲染与图标工厂等共享能力。

## 2. Java 用户如何使用

以静态方法直接调用，无需实例化。

## 3. 开源代码实际用法调研

以 OpenJDK 源码（JDK-27 桌面模块 java.desktop）为准：

- 定义位置：`src/java.desktop/share/classes/javax/swing/plaf/basic/BasicIconFactory.java:53`
- 实际引用位置：`src/java.desktop/share/classes/javax/swing/plaf/basic/BasicIconFactory.java:65`

## 4. 测试用例设计 + 自动化实现

- 白盒（testWhiteBox）：通过 Class.forName 反射校验类的可加载性、修饰符、父类与实现的接口；
- 黑盒（testBlackBox）：直接 实例化（或经工厂/反射获取）并组合调用关键方法，断言返回值与副作用；
- 自动化：采用 jtreg 单文件测试，纯数据/接口用例直接在主线程在事件分发线程（EDT）上运行，结束后释放组件。
