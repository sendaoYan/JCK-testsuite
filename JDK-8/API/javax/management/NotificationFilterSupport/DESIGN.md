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

## 深化（2026-11，批次 d）
- 前缀语义锁定：匹配为 String.startsWith——enable "jck." 后 "jck.x" 为 true、"jck"（更短）为 false、"other.jck.x" 为 false（无子串/后缀语义）；'*' 是字面字符（enable "jck.*" 不匹配 "jck.x"，仅匹配字面 "jck.*"）；enableType("") 后任意非 null 类型全部通过；启用更长前缀不匹配更短类型。
- null 矩阵：isNotificationEnabled(null) → NullPointerException（实现先 notification.getType()，null 通知直接 NPE）；type==null 恒 false；enableType(null) → IllegalArgumentException；disableType(未知名) 与 disableType(null) 均为 no-op 不抛。
- getEnabledTypes 返回活 Vector：经返回值 add 可写穿过滤器集合（随后新前缀参与匹配）——不是不可变视图也不是快照，两侧一致锁定。
- 序列化 round-trip：enabled 集大小/内容与匹配/拒绝行为全保留。

### 版本差异
- 无实现差异：jdk8u-dev 与主线 jdk 源码逐行一致（startsWith 匹配 + 返回同一 Vector 引用）。
