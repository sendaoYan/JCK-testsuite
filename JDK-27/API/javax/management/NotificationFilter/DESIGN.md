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

## 深化（2026-11，批次 d）
- 接口形状：恰 1 个声明方法 isNotificationEnabled(Notification) 返回 boolean、无受检异常；extends Serializable；NotificationFilterSupport 与 AttributeChangeNotificationFilter instanceof 断言；lambda 可用。
- 过滤分发闭环：自造可序列化 RecordingFilter 经 NotificationBroadcasterSupport——filter 被以同一（==）通知实例咨询；false 分支不投递；true 分支投递同一实例；抛 IllegalStateException 的 filter 被 sendNotification 捕获跳过（不向调用方传播、其余监听器照常收到）；自定义 filter 序列化 round-trip 后匹配行为保持。

### 版本差异
- 无（过滤器异常容错为 sendNotification 的 catch(Exception)+continue，两侧实现一致）。
