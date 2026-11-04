# java.awt.LinearGradientPaint 测试设计

## 1. API 功能
线性渐变填充，继承 MultipleGradientPaint，由起止点、fractions、colors 定义。

## 2. Java 用户如何使用
多个构造（float 坐标 / Point2D / CycleMethod / ColorSpaceType / AffineTransform）；getStartPoint/getEndPoint/getFractions/getColors/getCycleMethod/getColorSpace/getTransform/getTransparency/createContext。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/LinearGradientPaint.java

## 4. 测试用例设计 + 自动化实现
白盒: final、继承 MultipleGradientPaint；黑盒: 各构造、getter 值、createContext 非空、CycleMethod。

自动化实现位于同目录 `LinearGradientPaintTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
