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

## 深化（2026-11，批次 d）
- 精确名匹配锁定：equals 精确匹配 attributeName——enable "Count" 后 "Count" true、"CountX"/"Cou"（无前缀语义）/"count"（大小写敏感）/""（空名）false；显式 enableAttribute("") 后空属性名可匹配；非 AttributeChangeNotification 实例即使 type 串为 "jmx.attribute.change" 也拒绝（instanceof 检查）。
- null 矩阵：isNotificationEnabled(null) → NullPointerException（实现先读 getType() 再 instanceof，null 通知在第一行 NPE）；enableAttribute(null) → IllegalArgumentException；disableAttribute(未知名) 与 disableAttribute(null) 均 no-op 不抛。
- getEnabledAttributes 返回活 Vector：经返回值 remove 可写穿（该属性随后不再匹配），非快照，两侧一致锁定。
- 序列化 round-trip：enabled 名集内容与匹配/拒绝/非 ACN 拒绝行为全保留。

### 版本差异
- 无实现差异：jdk8u-dev 与主线 jdk 源码一致（含"先 getType 后 instanceof"导致的 null → NPE 同一顺序）。
