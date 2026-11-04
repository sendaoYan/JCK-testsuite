# javax.management.AttributeChangeNotification 黑盒行为测试设计

## 1. API 功能
- 常量：`ATTRIBUTE_CHANGE`（"jmx.attribute.change"）。
- 构造：`(Object source, long sequenceNumber, long timeStamp, String msg, String attributeName, String attributeType, Object oldValue, Object newValue)`。
- 查询：`getAttributeName()`、`getAttributeType()`、`getOldValue()`、`getNewValue()`。

## 2. Java 用户如何使用
当 MBean 属性变化时构造该通知，监听方用 `getOldValue/getNewValue` 取变化前后值。
```java
AttributeChangeNotification n = new AttributeChangeNotification(src, seq, ts, msg,
        "Count", "int", oldVal, newVal);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/notification/NotifInfoTest.java`：通知类型定义。
- `test/jdk/javax/management/monitor/*`：属性变化通知的消费。
- 实现权威：`src/java.management/share/classes/javax/management/AttributeChangeNotification.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：构造后 getAttributeName/getAttributeType/getOldValue/getNewValue 返回入参；getType 等于常量 ATTRIBUTE_CHANGE；继承 Notification 的 getMessage/getSource/getSequenceNumber/getTimeStamp。
- 白盒：public 类、父类为 Notification。
