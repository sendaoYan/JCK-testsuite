# java.awtPaintContext 黑盒行为测试设计

## 1. API 功能
漆上下文接口：getColorModel/getRaster(int,int)+dispose；经各 Paint.createContext 返回的上下文 getColorModel 一致性、getRaster 可用（宽度≥2 放宽）。

## 2. Java 用户如何使用
```java
ctx.getRaster(0,0,4,4)
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 PaintContext.java；GradientPaint context 光栅宽度随环境 2~4（放宽断言）。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：与 Paint 联测：上下文非空；getRaster 宽度≥2；dispose。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
