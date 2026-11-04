# java.awtStroke 黑盒行为测试设计

## 1. API 功能
线型接口：唯一方法 createStrokedShape(Shape)；BasicStroke 实现——加宽后包围盒高度随 width 增大（1 vs 5）。

## 2. Java 用户如何使用
```java
Shape outline = new BasicStroke(5f).createStrokedShape(line)
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 Stroke.java；BasicStroke 输出轮廓形状。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：接口单方法；两 stroke 输出非空；包围盒高度 1→5 单调增大。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
