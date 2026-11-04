# javax.management.NotificationFilter 黑盒行为测试设计

## 1. API 功能
- 接口：`isNotificationEnabled(Notification)`。

## 2. Java 用户如何使用
注册监听器时传入过滤器，决定通知是否送达。
```java
boolean deliver = filter.isNotificationEnabled(notification);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/notification/FilterExceptionTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/NotificationFilter.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：用 NotificationFilterSupport 作实现验证 isNotificationEnabled 对 enabled 类型为 true、对其它为 false。
- 白盒：public 接口、含 isNotificationEnabled。
