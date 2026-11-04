# javax.management.NotificationBroadcaster 黑盒行为测试设计

## 1. API 功能
- 接口：`addNotificationListener(NotificationListener, NotificationFilter, Object)`、`removeNotificationListener(NotificationListener)`、`getNotificationInfo()`。

## 2. Java 用户如何使用
通知源实现此接口；客户端注册监听器接收通知。
```java
broadcaster.addNotificationListener(listener, filter, handback);
broadcaster.removeNotificationListener(listener);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/notification/*Test.java`：监听器注册/过滤/移除。
- 实现权威：`src/java.management/share/classes/javax/management/NotificationBroadcaster.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：用 NotificationBroadcasterSupport 作实现验证 addNotificationListener/getNotificationInfo/removeNotificationListener 契约。
- 白盒：public 接口、含 addNotificationListener/removeNotificationListener/getNotificationInfo。

## 深化（2026-11，批次 d）
- 精确签名锁定：接口恰好 3 个声明方法；addNotificationListener(NotificationListener,NotificationFilter,Object) 声明 IllegalArgumentException；removeNotificationListener(NotificationListener) 声明 ListenerNotFoundException；getNotificationInfo() 返回 `[Ljavax.management.MBeanNotificationInfo;`；两个改动型方法返回 void。
- 接口引用闭环（NotificationBroadcasterSupport 实例以 NotificationBroadcaster 引用驱动）：注入 MBeanNotificationInfo 经接口读出内容 + 返回数组为防御克隆；双监听器各收到同一（==）通知与各自 handback；接口 remove 后停发；remove 未注册 → ListenerNotFoundException；接口 add null listener → IllegalArgumentException；NotificationEmitter is-a NotificationBroadcaster。

### 版本差异
- 无（双侧 javap 签名与行为一致）。
