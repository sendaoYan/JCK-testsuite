# javax.management.relation.MBeanServerNotificationFilter — 设计案

## 1. API 功能
- 继承 `NotificationFilterSupport`，用于过滤 `MBeanServerNotification`。
- 构造器启用 `REGISTRATION_NOTIFICATION` 与 `UNREGISTRATION_NOTIFICATION` 两类通知。
- 选择逻辑：`enableObjectName`/`disableObjectName`/`enableAllObjectNames`/`disableAllObjectNames`。
- 查询：`getEnabledObjectNames()`、`getDisabledObjectNames()`、`getEnabledTypes()`（继承）。
- 判定：`isNotificationEnabled(Notification)` 组合类型匹配与 ObjectName 选择。

## 2. Java 用户如何使用
典型组合链路：
```java
MBeanServerNotificationFilter f = new MBeanServerNotificationFilter();
f.disableAllObjectNames();
f.enableObjectName(watched);
if (f.isNotificationEnabled(n)) { /* 转发 */ }
```

## 3. 开源代码实际用法调研
- JDK 源码自用：`javax.management.relation.RelationService` 内部使用 `MBeanServerNotificationFilter myUnregNtfFilter`，`enableObjectName` 注册对引用 MBean 的监听（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation/RelationService.java`）。
- 说明：该类主要由 JMX 关系服务内部使用，开源生态偶见于对 MBeanServer 注册/注销事件的自定义监听过滤。

## 4. 测试用例设计
- 白盒：public 类、父类 NotificationFilterSupport、七个关键方法存在。
- 黑盒：构造默认启用两类通知、`enableAll`/`disableAll` 对 ObjectName 集与 `isNotificationEnabled` 的影响、`enableObjectName`/`disableObjectName` 组合、非 MBeanServer 类型拒绝、三处 null 参数精确 message。
