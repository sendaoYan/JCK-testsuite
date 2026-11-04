# javax.management.modelmbean.ModelMBeanNotificationBroadcaster — 设计案

## 1. API 功能
- 接口，扩展 `NotificationBroadcaster`。
- 核心方法：`addAttributeChangeNotificationListener`/`removeAttributeChangeNotificationListener`、`sendAttributeChangeNotification(AttributeChangeNotification)`、`sendAttributeChangeNotification(Attribute oldValue, Attribute newValue)`、`sendNotification(String)`、`sendNotification(Notification, Object)`。

## 2. Java 用户如何使用
ModelMBean 通过该接口向监听器推送属性变更通知与任意 JMX 通知。
```java
broadcaster.addAttributeChangeNotificationListener(listener, attrName, handback);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/modelmbean/` 系列覆盖通知注册与派发。
- 说明：接口把 `NotificationBroadcaster` 扩展到属性级过滤的监听器注册与属性变更通知，测试聚焦监听器增删与可组合性。

## 4. 测试用例设计
- 白盒：接口、继承 NotificationBroadcaster、方法含 add/removeAttributeChangeNotificationListener 与 send* 族。
- 黑盒：用 RequiredModelMBean 等实现验证 add/remove 监听器往返、remove 后不再计数、sendNotification 等最小契约。
