# java.awtTransparency 黑盒行为测试设计

## 1. API 功能
透明度接口：OPAQUE=1/BITMASK=2/TRANSLUCENT=3；Color 红 OPAQUE、半透明 TRANSLUCENT；TYPE_INT_ARGB 图像与其 ColorModel TRANSLUCENT、TYPE_INT_RGB OPAQUE。

## 2. Java 用户如何使用
```java
bi.getColorModel().getTransparency() == Transparency.TRANSLUCENT
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 Transparency.java 常量定义。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：三常量精确；Color 两分支；BufferedImage 两类型；ColorModel 委托。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
