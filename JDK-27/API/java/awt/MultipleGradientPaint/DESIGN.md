# java.awt.MultipleGradientPaint 测试设计

## 1. API 功能
渐变填充抽象基类，实现 Paint，定义 fractions/colors/cycle/colorSpace/transform。

## 2. Java 用户如何使用
不可直接实例化（包私有构造）；由 LinearGradientPaint/RadialGradientPaint 继承并暴露 getFractions/getColors/getCycleMethod/getColorSpace/getTransform/getTransparency。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/MultipleGradientPaint.java

## 4. 测试用例设计 + 自动化实现
白盒: 抽象、implements Paint、声明 6 个 getter；黑盒降级: 无法直接实例化（包私有构造），getter 在子类测试覆盖。

自动化实现位于同目录 `MultipleGradientPaintTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
