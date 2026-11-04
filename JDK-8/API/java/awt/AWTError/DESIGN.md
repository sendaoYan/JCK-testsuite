# java.awtAWTError 黑盒行为测试设计

## 1. API 功能
严重 AWT 错误：Error 子类、消息保留、无参构造 null 消息、不可捕获恢复语义。

## 2. Java 用户如何使用
```java
throw new AWTError("toolkit not found")
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 AWTError.java：单一 String 构造器。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：消息保留；Error 层级；toString 含消息。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
