# java.awtCompositeContext 黑盒行为测试设计

## 1. API 功能
合成上下文接口：compose(srcRaster,dstRaster,outRaster) 像素级合成 + dispose；经 AlphaComposite.createContext 实测。

## 2. Java 用户如何使用
```java
c.compose(src.getRaster(), dst.getRaster(), dst.getRaster())
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 CompositeContext.java；AlphaComposite context 实现 SunCompositeContext。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：同 Composite 联测：SRC_OVER 透明底出 0xFF804020 精确像素；dispose；接口方法存在。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
