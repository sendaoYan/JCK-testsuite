# javax.management.NotificationFilter 契约测试设计

## 1. API 功能
- 接口，扩展 java.io.Serializable，仅一个方法：`isNotificationEnabled(Notification)` 返回 boolean、无受检异常。
- 内置实现：NotificationFilterSupport（类型前缀）、AttributeChangeNotificationFilter（属性名精确）。

## 2. Java 用户如何使用
注册监听器时传入过滤器，决定通知是否送达；过滤器在发送线程被咨询（NotificationBroadcasterSupport 语义）。
```java
broadcaster.addNotificationListener(listener, filter, handback);
```

## 3. 参考来源
- Oracle javadoc：javax.management.NotificationFilter。
- OpenJDK `test/jdk/javax/management/notification/FilterExceptionTest.java`（过滤器抛异常的容错思路，代码自写）。
- 实现权威：`src/share/classes/javax/management/NotificationBroadcasterSupport.java` sendNotification 的 `catch (Exception e) { continue; }` 分支。

## 4. 测试用例设计 + 易错规范细节
- 白盒：public 接口、extends Serializable、恰 1 个声明方法、返回 boolean、无受检异常；两个内置实现 instanceof 断言；lambda 可用。
- 过滤分发闭环：自造可序列化 RecordingFilter 经 NotificationBroadcasterSupport 分发——filter 被以同一（==）通知实例咨询；false 分支不投递；true 分支投递同一实例。
- 抛异常容错：filter 抛 IllegalStateException → sendNotification 不向调用方传播，该监听器被跳过，其余监听器照常收到。
- 序列化：自定义 Serializable filter round-trip 后匹配行为保持。
- 易错点：过滤器异常不会中断 sendNotification（逐监听器容错）；过滤发生在发送线程而非监听线程。

## 深化（2026-11，批次 d）
- 接口形状：恰 1 个声明方法 isNotificationEnabled(Notification) 返回 boolean、无受检异常；extends Serializable；NotificationFilterSupport 与 AttributeChangeNotificationFilter instanceof 断言；lambda 可用。
- 过滤分发闭环：自造可序列化 RecordingFilter 经 NotificationBroadcasterSupport——filter 被以同一（==）通知实例咨询；false 分支不投递；true 分支投递同一实例；抛 IllegalStateException 的 filter 被 sendNotification 捕获跳过（不向调用方传播、其余监听器照常收到）；自定义 filter 序列化 round-trip 后匹配行为保持。

### 版本差异
- 无（过滤器异常容错为 sendNotification 的 catch(Exception)+continue，两侧实现一致）。
