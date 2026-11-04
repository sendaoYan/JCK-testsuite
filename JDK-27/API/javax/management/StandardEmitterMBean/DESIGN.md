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
