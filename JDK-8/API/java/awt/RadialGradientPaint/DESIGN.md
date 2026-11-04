# java.awtRadialGradientPaint 黑盒行为测试设计

## 1. API 功能
径向渐变：圆心/焦点（默认等于圆心）、radius 0/负 IAE、乱序 fractions IAE、null NPE、远焦点被接受（观察）、全参读回、transparency、createContext。

## 2. Java 用户如何使用
```java
new RadialGradientPaint(center, 10f, new float[]{0,1}, colors)
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 RadialGradientPaint.java：焦点在圆外时行为未定义但实现接受。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：圆心/焦点默认；radius 边界 IAE；乱序 IAE；NPE；远焦点接受（观察锁定）；全参读回；transparency；createContext。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
