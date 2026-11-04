# java.awtAWTPermission 黑盒行为测试设计

## 1. API 功能
AWT 权限：BasicPermission 语义——getActions 恒空串、implies 通配、null 名 NPE、仅目标名无动作。

## 2. Java 用户如何使用
```java
new AWTPermission("fullScreenExclusive").implies(new AWTPermission("*"))
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 BasicPermission.java：名字匹配通配规则。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：getActions 空；implies 通配/前缀；null NPE；层次。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
