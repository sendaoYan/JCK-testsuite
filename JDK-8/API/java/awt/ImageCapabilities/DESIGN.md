# java.awtImageCapabilities 黑盒行为测试设计

## 1. API 功能
图像能力值对象：isAccelerated 往返、isTrueVolatile false、无 equals 重写（恒等语义）、clone。

## 2. Java 用户如何使用
```java
new ImageCapabilities(true).isAccelerated()
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 ImageCapabilities.java：仅两布尔查询，无 equals。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：accelerated 往返；isTrueVolatile false；无 equals（恒等）；clone 可用。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
