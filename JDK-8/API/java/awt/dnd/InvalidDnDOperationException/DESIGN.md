# java.awtdnd/InvalidDnDOperationException 黑盒行为测试设计

## 1. API 功能
拖放非法状态异常：extends IllegalStateException、无参构造默认消息非空、消息构造保留、无 cause。

## 2. Java 用户如何使用
```java
throw new InvalidDnDOperationException("drag in progress")
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 InvalidDnDOperationException.java。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：层级；两构造器；cause null。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
