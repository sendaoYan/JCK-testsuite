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

## 深化（2026-11，批次 d）
- type 硬编码：构造无 type 参数，getType() 恒为 "jmx.attribute.change"（与 ATTRIBUTE_CHANGE 常量同一字符串）；null attributeName/attributeType 构造原样保留（无校验）。
- 值恒等：getOldValue/getNewValue 返回构造传入的同一（==）实例；userData 不被构造自动填充；setUserData 与 newValue 字段相互独立；attributeType 是不透明字符串（任意串原样往返）。
- 广播+过滤闭环：经 NotificationBroadcasterSupport（info 声明 jmx.attribute.change）+ AttributeChangeNotificationFilter(enable "Count")——非启用属性（"Other"）的 ACN 被过滤（有界静默窗口断言不达）；启用属性（"Count"）的 ACN 以同一（==）实例投递且 attrName/attrType/old/new 全保留；handback 同一实例回传。
- toString 沿用 Notification 格式：前缀 `javax.management.AttributeChangeNotification[source=..]` + `[type=jmx.attribute.change][message=..]` 锁定。

### 版本差异
- 无行为差异；JDK-27 侧本批补齐序列化 round-trip（attrName/attrType/old/new/seq/ts/type 全保留，JDK-8 侧已有）。
