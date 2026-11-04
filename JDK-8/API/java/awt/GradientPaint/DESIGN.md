# java.awtGradientPaint 黑盒行为测试设计

## 1. API 功能
线性渐变漆：cyclic 标志、端点/颜色读回、不透明/半透明 transparency、createContext headless、getRaster 宽度≥2（放宽）、非 Serializable。

## 2. Java 用户如何使用
```java
new GradientPaint(0,0,red,10,10,blue,true) 循环渐变
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 GradientPaint.java：双点+cyclic；context 由 ColorModel 派生。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：cyclic 往返；端点读回；transparency 两分支；createContext；getRaster ≥2；非 Serializable。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
