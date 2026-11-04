# java.awtGraphicsConfigTemplate 黑盒行为测试设计

## 1. API 功能
图形配置模板：REQUIRED=1/PREFERRED=2/UNNECESSARY=3、抽象类、Serializable、两个抽象方法 getBestConfiguration/isGraphicsConfigSupported 子类可测。

## 2. Java 用户如何使用
```java
class T extends GraphicsConfigTemplate { ... }
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 GraphicsConfigTemplate.java：常量供 GraphicsDevice.getConfigurations 筛选。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：三常量；抽象；Serializable；子类行为（空数组 null、null gc false）。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
