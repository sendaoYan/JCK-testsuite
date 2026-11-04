# javax.management.NotificationFilterSupport 黑盒行为测试设计

## 1. API 功能
- 实现 NotificationFilter：`enableType(String)`、`disableType(String)`、`disableAllTypes()`、`getEnabledTypes()`、`isNotificationEnabled(Notification)`。

## 2. Java 用户如何使用
按通知类型前缀过滤通知，注册监听器时传入。
```java
NotificationFilterSupport f = new NotificationFilterSupport();
f.enableType("jmx.attribute.change");
emitter.addNotificationListener(listener, f, null);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/notification/FilterExceptionTest.java`。
- `test/jdk/javax/management/MBeanServer/MBeanServerNotificationTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/NotificationFilterSupport.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：初始 enabled 为空；enableType 后 isNotificationEnabled 对匹配前缀为 true、不匹配为 false；disableType 移除；disableAllTypes 清空；getEnabledTypes 返回按字典序的向量；配合 NotificationBroadcasterSupport 验证过滤分发。
- 白盒：public 类、implements NotificationFilter、含 enableType/disableType/isNotificationEnabled。
