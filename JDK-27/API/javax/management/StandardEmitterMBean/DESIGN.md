# javax.management.StandardEmitterMBean 黑盒行为测试设计

## 1. API 功能
- 继承 StandardMBean，实现 NotificationEmitter。
- 构造：`(Class<T> mbeanInterface, boolean isMXBean, NotificationEmitter emitter)` 及 `(T implementation, Class<T>, boolean, NotificationEmitter)`。

## 2. Java 用户如何使用
需要标准 MBean 元数据 + 通知发射能力的 MBean 基类；把 NotificationBroadcasterSupport 作为 emitter 传入。
```java
StandardEmitterMBean m = new StandardEmitterMBean(impl, ThingMBean.class, false, broadcasterSupport);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/mxbean/StandardMBeanOverrideTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/StandardEmitterMBean.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getMBeanInterface/getImplementationClass；getMBeanInfo 内省出属性/操作；addNotificationListener 后 sendNotification 通过 emitter 分发；getNotificationInfo 来自 emitter。
- 白盒：public 类、父类 StandardMBean、implements NotificationEmitter、含多个构造器。

## 深化（2026-11，批次 e）
新增维度：emitter 注入与 MBeanInfo 合并、注册后通知真闭环（有界轮询）、filter/handback、listener 移除精度矩阵。
- getNotificationInfo() 与 getMBeanInfo().getNotifications() 均返回构造注入 emitter 的**同一 MBeanNotificationInfo 实例**；3 参与 4 参（allowPublicMethods=false）构造等价。
- 通知闭环：注册进 newMBeanServer() 后 server.addNotificationListener → sendNotification → listener 收到**同一 Notification 实例**（== 断言）+ handback 同一回传；属性/操作路由与 StandardMBean 一致（Count 读写、Ready isIs、add）。
- filter isNotificationEnabled=false → 不投递；换接受 filter（remove 3 参 + 重加）后恢复投递。
- 移除精度：错 filter/handback/listener 的 3 参 remove → ListenerNotFoundException；精确 (listener,filter,handback) 移除成功；重复移除/1 参移除未注册 → ListenerNotFoundException；server.addNotificationListener(name, null, ...) → RuntimeOperationsException（"Null listener"）。
### 版本差异
- 无（两侧行为一致）。
