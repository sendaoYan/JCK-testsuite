# java.awtAWTKeyStroke 黑盒行为测试设计

## 1. API 功能
键击描述：工厂缓存同实例、getModifiers()=65（SHIFT 新旧掩码合并）、字符串解析（ctrl/control/typed/released/组合）、修饰词大小写敏感（'ctrl ALT X' IAE）、null/空/乱串 IAE、负 keyCode 被接受、getAWTKeyStrokeForEvent 三态、equals/hashCode、toString 'ctrl pressed X'、序列化保 onKeyRelease/modifiers。

## 2. Java 用户如何使用
```java
AWTKeyStroke.getAWTKeyStroke('ctrl X'); ks.isOnKeyRelease()
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 AWTKeyStroke.java：注册表缓存；getModifiers 合并 downMask 与旧掩码位。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：缓存同实例；65 合并；解析矩阵；大小写敏感 IAE；负 keyCode -1 读回；ForEvent 三态；equals/hashCode；toString 精确；序列化往返。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
