# java.awt.RadialGradientPaint 测试设计

## 1. API 功能
径向渐变填充，由圆心、半径、fractions、colors 定义。

## 2. Java 用户如何使用
多个构造（float / Point2D / 焦距 / CycleMethod / ColorSpaceType / Rectangle2D）；getCenterPoint/getFocusPoint/getRadius/getFractions/getColors/.../createContext。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/RadialGradientPaint.java

## 4. 测试用例设计 + 自动化实现
白盒: final、继承 MultipleGradientPaint；黑盒: 各构造、getter、createContext。

自动化实现位于同目录 `RadialGradientPaintTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
