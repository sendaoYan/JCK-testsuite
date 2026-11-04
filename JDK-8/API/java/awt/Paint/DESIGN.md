# java.awtPaint 黑盒行为测试设计

## 1. API 功能
漆接口：extends Transparency + createContext(ColorModel,Rectangle,Rectangle2D,AffineTransform,RenderingHints)；Color/Gradient/Linear/Radial 四实现 headless 建上下文、不透明 getTransparency==OPAQUE、半透明 Color TRANSLUCENT。

## 2. Java 用户如何使用
```java
Paint p = new GradientPaint(..); p.createContext(cm, devRect, ub, xform, hints)
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 Paint.java；四实现 context 工厂实测。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：接口层级；四实现 createContext 非空；OPAQUE 四例；半透明分支；TexturePaint 属同族。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
