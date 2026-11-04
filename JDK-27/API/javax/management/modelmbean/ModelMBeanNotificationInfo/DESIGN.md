# javax.management.modelmbean.ModelMBeanNotificationInfo — 设计案

## 1. API 功能
- 具体值类，继承 `MBeanNotificationInfo` 并实现 `DescriptorAccess`。
- 构造器：`(String[] notifTypes, String name, String description)`、带 Descriptor 的 `(String[],String,String,Descriptor)`、拷贝构造。
- Descriptor 默认含 `name`（通知类名）与 `descriptorType=notification`。

## 2. Java 用户如何使用
描述 ModelMBean 会发出的通知类型集合。
```java
ModelMBeanNotificationInfo n = new ModelMBeanNotificationInfo(
    new String[]{"test.notif"}, "javax.management.Notification", "desc");
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/modelmbean/ModelMBeanNotificationInfoTest.java` 覆盖构造器、clone、setDescriptor。
- 实现权威：`src/java.management/share/classes/javax/management/modelmbean/ModelMBeanNotificationInfo.java`。

## 4. 测试用例设计
- 白盒：继承 MBeanNotificationInfo、实现 DescriptorAccess、多个 public 构造器。
- 黑盒：基础构造器 name/description/notifTypes 往返；descriptor 默认 name/descriptorType；带 Descriptor 构造器保留 extra；拷贝构造 equals；clone 相等且保留类型；setDescriptor 更新。
