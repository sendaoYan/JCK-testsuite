# javax.management.NotificationBroadcaster 契约测试设计

## 1. API 功能
- 接口（无父接口）：`addNotificationListener(NotificationListener, NotificationFilter, Object)`、`removeNotificationListener(NotificationListener)`、`getNotificationInfo()`。
- 恰好 3 个方法；add 声明 IllegalArgumentException，remove 声明 ListenerNotFoundException。

## 2. Java 用户如何使用
任何通知源（MBean 或普通对象）实现此接口即可接收监听器注册；通常直接复用 NotificationBroadcasterSupport。
```java
NotificationBroadcaster b = new NotificationBroadcasterSupport(info);
b.addNotificationListener(listener, filter, handback);
```

## 3. 参考来源
- Oracle javadoc：javax.management.NotificationBroadcaster（接口契约与异常声明）。
- 实现权威：`src/share/classes/javax/management/NotificationBroadcaster.java`（OpenJDK jdk8u-dev）。
- 双侧 `javap -public` 对照：JDK-8 与 JDK-27 公开签名一致。

## 4. 测试用例设计 + 易错规范细节
- 白盒：public 接口、无 superclass、三个方法齐全；精确签名（参数类型逐个比对、返回类型 [Ljavax.management.MBeanNotificationInfo;、void、受检异常逐一断言）、恰好 3 个声明方法。
- 行为（经 NotificationBroadcasterSupport 以接口引用驱动）：注入 MBeanNotificationInfo 后经接口读内容 + 返回数组为防御克隆；双监听器各收到同一（==）通知与各自 handback；接口 remove 后停发；remove 未注册 → ListenerNotFoundException；add null listener → IllegalArgumentException。
- 易错点：getNotificationInfo() 每次返回克隆，改它不影响源；remove 的异常在接口层就声明。

## 深化（2026-11，批次 d）
- 精确签名锁定：接口恰好 3 个声明方法；addNotificationListener(NotificationListener,NotificationFilter,Object) 声明 IllegalArgumentException；removeNotificationListener(NotificationListener) 声明 ListenerNotFoundException；getNotificationInfo() 返回 `[Ljavax.management.MBeanNotificationInfo;`；两个改动型方法返回 void。
- 接口引用闭环（NotificationBroadcasterSupport 实例以 NotificationBroadcaster 引用驱动）：注入 MBeanNotificationInfo 经接口读出内容 + 返回数组为防御克隆；双监听器各收到同一（==）通知与各自 handback；接口 remove 后停发；remove 未注册 → ListenerNotFoundException；接口 add null listener → IllegalArgumentException；NotificationEmitter is-a NotificationBroadcaster。

### 版本差异
- 无（双侧 javap 签名与行为一致）。
