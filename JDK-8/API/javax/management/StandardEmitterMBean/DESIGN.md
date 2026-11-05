# javax.management.StandardEmitterMBean 契约测试设计

## 1. API 功能
- StandardMBean 的通知发射变体：构造 `(impl, iface, NotificationEmitter)`、`(impl, iface, boolean, emitter)`；add/removeNotificationListener（1/3 参）、getNotificationInfo、sendNotification。

## 2. Java 用户典型用法
```java
NotificationBroadcasterSupport emitter = new NotificationBroadcasterSupport(ninfo);
StandardEmitterMBean m = new StandardEmitterMBean(impl, CounterMBean.class, emitter);
server.registerMBean(m, on);
m.sendNotification(new Notification("t", on, seq));
```

## 3. 参考来源
- Oracle javadoc：javax.management.StandardEmitterMBean（通知信息取自 emitter）。
- OpenJDK `test/jdk/javax/management/notification/`（思路借鉴）。

## 4. 测试用例设计
- 既有：结构反射（超类/接口/方法）。

## 深化（2026-11，批次 e）
新增维度：emitter 注入与 MBeanInfo 合并、注册后通知真闭环（有界轮询）、filter/handback、listener 移除精度矩阵。
- getNotificationInfo() 与 getMBeanInfo().getNotifications() 均返回构造注入 emitter 的**同一 MBeanNotificationInfo 实例**；3 参与 4 参（allowPublicMethods=false）构造等价。
- 通知闭环：注册进 newMBeanServer() 后 server.addNotificationListener → sendNotification → listener 收到**同一 Notification 实例**（== 断言）+ handback 同一回传；属性/操作路由与 StandardMBean 一致（Count 读写、Ready isIs、add）。
- filter isNotificationEnabled=false → 不投递；换接受 filter（remove 3 参 + 重加）后恢复投递。
- 移除精度：错 filter/handback/listener 的 3 参 remove → ListenerNotFoundException；精确 (listener,filter,handback) 移除成功；重复移除/1 参移除未注册 → ListenerNotFoundException；server.addNotificationListener(name, null, ...) → RuntimeOperationsException（"Null listener"）。
### 版本差异
- 无（两侧行为一致）。
