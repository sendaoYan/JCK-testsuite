# javax.swing.plaf.ToolBarUI

## 1. API 功能

Swing 外观（Look & Feel）委托抽象基类，为具体外观实现定义组件级绘制与行为契约。

## 2. Java 用户如何使用

通过组件的 getUI() 获取，或由具体外观的 createUI(Component) 工厂方法创建并 installUI/uninstallUI。

## 3. 开源代码实际用法调研

以 OpenJDK 源码（JDK-27 桌面模块 java.desktop）为准：

- 定义位置：`src/java.desktop/share/classes/javax/swing/plaf/ToolBarUI.java:37`
- 实际引用位置：`src/java.desktop/share/classes/javax/swing/plaf/ToolBarUI.java:41`

## 4. 测试用例设计 + 自动化实现

- 白盒（testWhiteBox）：通过 Class.forName 反射校验类的可加载性、修饰符、父类与实现的接口；
- 黑盒（testBlackBox）：以最小匿名子类 `new ToolBarUI() {}` 直接调用受保护的 `<init>()`，再对未显示组件与离屏 Graphics 组合调用继承自 ComponentUI 的可观察方法（installUI/paint/update/uninstallUI/getPreferredSize/getMinimumSize/getMaximumSize/contains/getBaseline/getBaselineResizeBehavior/getAccessibleChildrenCount/getAccessibleChild），断言默认返回值形状（null / false / -1 / OTHER / 0）与 null 组件、负宽参数的参数契约；
- 自动化：采用 jtreg 单文件测试，纯 headless（无 @key），不 new JFrame / setVisible，不依赖显示或 EDT。
