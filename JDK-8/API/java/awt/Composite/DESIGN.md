# java.awtComposite 黑盒行为测试设计

## 1. API 功能
合成接口：仅 createContext(ColorModel,ColorModel,RenderingHints)；AlphaComposite 为旗舰实现——SrcOver 上下文可 dispose；CompositeContext 接口 compose(Raster,Raster,Raster) 实测 SRC_OVER 透明底出源像素 0xFF804020。

## 2. Java 用户如何使用
```java
CompositeContext c = AlphaComposite.SrcOver.createContext(cmSrc, cmDst, hints)
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 Composite.java/CompositeContext.java；SRC_OVER 像素公式实测。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：接口声明；createContext 非空；compose 透明底精确像素；dispose 安全；实现关系。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
