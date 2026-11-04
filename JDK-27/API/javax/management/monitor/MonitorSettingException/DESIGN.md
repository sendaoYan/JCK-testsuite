# javax.management.monitor.MonitorSettingException — 设计案

## 1. API 功能
- 单一异常类，继承 `javax.management.JMRuntimeException`（再上为 RuntimeException）。
- 构造器：无参 + `(String message)`，用于表示监视器配置非法。

## 2. Java 用户如何使用
监视器设置非法参数时抛出/捕获。
```java
try { monitor.setGranularityPeriod(-1); }
catch (MonitorSettingException e) { ... }
```

## 3. 开源代码实际用法调研
- JDK `src/java.management/share/classes/javax/management/monitor/` 内部抛出该异常。
- 说明：纯异常载体，测试聚焦构造器、消息传递与继承链。

## 4. 测试用例设计
- 白盒：具体类、继承 JMRuntimeException、父类直指 JMRuntimeException、2 个 public 构造器。
- 黑盒：String 构造器 message 原样返回；无参构造器 message 为 null；instanceof JMRuntimeException/RuntimeException。
