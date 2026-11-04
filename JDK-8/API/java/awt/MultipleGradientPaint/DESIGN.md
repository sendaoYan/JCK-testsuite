# java.awtMultipleGradientPaint 黑盒行为测试设计

## 1. API 功能
渐变基类：CycleMethod 3 值+valueOf、ColorSpaceType 2 值、抽象类、子类层级（Linear/Radial 继承）、基类访问器委托。

## 2. Java 用户如何使用
```java
MultipleGradientPaint.CycleMethod.REPEAT / ColorSpaceType.LINEAR_RGB
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 MultipleGradientPaint.java：抽象基类持有 fractions/colors/透明度。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：枚举值与 valueOf；抽象检查；Linear/Radial 是子类；基类访问器与子类一致。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
