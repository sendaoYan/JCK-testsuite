# java.awtAWTException 黑盒行为测试设计

## 1. API 功能
可检查 AWT 异常：Exception 子类、消息保留、无 cause 默认。

## 2. Java 用户如何使用
```java
throw new AWTException("headless")
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 AWTException.java：单一 String 构造器。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：消息保留；Exception 层级；cause null。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
