# javax.management.MBeanNotificationInfo 黑盒行为测试设计

## 1. API 功能
- 构造：`(String[] notifTypes, String name, String description)` 及带 Descriptor 的重载。
- 查询：`getNotifTypes()`、继承的 name/description/getDescriptor；`clone()`、`equals()`。

## 2. Java 用户如何使用
声明 MBean 会发出的通知类型，供管理工具订阅。
```java
MBeanNotificationInfo ni = new MBeanNotificationInfo(
    new String[]{"jmx.attribute.change"}, Notification.class.getName(), "notif");
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanInfo/NotificationInfoTest.java`：通知类型。
- `test/jdk/javax/management/notification/NotifInfoTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanNotificationInfo.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getNotifTypes 返回入参数组；clone 等值；equals 对通知类型数组敏感；带 Descriptor 构造返回该值。
- 白盒：public 类、父类 MBeanFeatureInfo、implements Cloneable、含 getNotifTypes。
