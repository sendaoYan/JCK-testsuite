# javax.management.AttributeChangeNotificationFilter 黑盒行为测试设计

## 1. API 功能
- 实现 `NotificationFilter`。
- 方法：`enableAttribute(String)`、`disableAttribute(String)`、`disableAllAttributes()`、`getEnabledAttributes()`、`isNotificationEnabled(Notification)`。

## 2. Java 用户如何使用
给监听器加过滤，只接收指定属性名（或属性类型前缀）的 AttributeChangeNotification。
```java
AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
f.enableAttribute("Count");
emitter.addNotificationListener(listener, f, null);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/notification/FilterExceptionTest.java`：过滤器组合。
- `test/jdk/javax/management/monitor/*`：属性变化通知过滤。
- 实现权威：`src/java.management/share/classes/javax/management/AttributeChangeNotificationFilter.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：初始 enabled 集合为空；enableAttribute 后 isNotificationEnabled 对匹配属性名/类型前缀为 true、对不匹配为 false；disableAllAttributes 清空；非 AttributeChangeNotification 返回 false。
- 白盒：public 类、implements NotificationFilter。
