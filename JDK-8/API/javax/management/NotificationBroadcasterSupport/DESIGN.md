# javax.management.NotificationBroadcasterSupport 黑盒行为测试设计

## 1. API 功能
- 实现 NotificationEmitter；构造可带 MBeanNotificationInfo 与 Executor。
- `sendNotification(Notification)`；protected `handleNotification(listener, notification, handback)`。

## 2. Java 用户如何使用
通知发送方的默认实现，MBean 继承它即可广播通知。
```java
class MyMBean extends NotificationBroadcasterSupport implements MyMBeanMBean {
    void fire() { sendNotification(new Notification("t", this, seq)); }
}
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/notification/BroadcasterSupportDeadlockTest.java`、`NotifExecutorTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/NotificationBroadcasterSupport.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getNotificationInfo 返回构造入参；addNotificationListener 后 sendNotification 分发同一通知与 handback；带 NotificationFilter 的监听器只收到匹配类型；removeNotificationListener 后不再收到；handleNotification 为 protected。
- 白盒：public 类、implements NotificationEmitter、含 sendNotification、protected handleNotification。

## 深化（2026-11，批次 d）
- 自造标准 MBean 闭环：嵌套 `BroadMBean`（getCount/setCount/reset/add）+ `Broad extends NotificationBroadcasterSupport implements BroadMBean`。命名规范：Standard MBean 内省要求接口二进制名 == <类名>+MBean；最初用 CounterMBean 触发 NotCompliantMBeanException（Broad 与 CounterMBean 名字不配对），改为 BroadMBean 后合规——该坑在测试注释中记录。
- Broad 注册到 `MBeanServerFactory.newMBeanServer()`（jck.broad:type=Broad）：server.getAttribute("Count") 读到真值；server.addNotificationListener(name,l,null,hb) 后 sendNotification → 监听器收到同一（==）通知 + handback 原样回传；server.removeNotificationListener(name,l,null,hb) 精确三元移除后停发。
- sendNotification 契约：已注册 (l,f,h) 收到同一（==）通知；filter false 分支经有界静默窗口（≤300ms/20ms 步）断言不达；handback 同一实例回传；removeNotificationListener(l) 停发、再 remove → ListenerNotFoundException；双参 remove 精确匹配（(l,f2,h2) 移除 (l,f1,h1) 注册 → ListenerNotFoundException；移除成功后再 remove → ListenerNotFoundException）；addNotificationListener(null,...) → IllegalArgumentException；sendNotification(null) 静默 no-op（不抛、不投递）。
- 多注册语义：同一 listener 以不同 handback 注册两次 → 每注册各投递一次；单参 removeNotificationListener(l) 是通配删除，一次清掉该 listener 全部注册（ListenerInfo wildcard equals 源码依据）。
- Executor 形态：每任务新起守护线程的 Executor；CountDownLatch 有界等待（≤5s）；断言跨线程收到同一（==）通知、handback 不变、handleNotification 运行线程 != 发送线程。
- getNotificationInfo：每次调用返回新克隆（两次返回数组引用不同、内容一致）。

### 版本差异
- JDK-8 与 JDK-27 公开 API（javap）与上述行为一致；JDK-27 侧另含既有白盒（protected handleNotification 等）。
