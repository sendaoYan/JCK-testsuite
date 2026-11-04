# javax.management.NotificationListener 黑盒行为测试设计

## 1. API 功能
- 接口：`handleNotification(Notification, Object handback)`。

## 2. Java 用户如何使用
实现该接口接收通知；handback 是注册时透传的对象。
```java
public void handleNotification(Notification n, Object handback) { ... }
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/notification/*Test.java`：监听器回调。
- `test/jdk/javax/management/MBeanServer/MBeanServerNotificationTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/NotificationListener.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：注册匿名监听器到 NotificationBroadcasterSupport，验证 handleNotification 收到同一通知与 handback。
- 白盒：public 接口、extends EventListener、含 handleNotification。

## 深化（2026-11，批次 d）
- 接口形状：恰 1 个声明方法 handleNotification(Notification,Object)，返回 void、无受检异常；extends EventListener；lambda（函数式接口）直接可用。
- 真 server 闭环（MBeanServerFactory.newMBeanServer）：监听 MBeanServerDelegate.DELEGATE_NAME；注册自造 Counter（嵌套 CounterMBean，类名/接口名满足 Standard MBean 配对约定）→ 收到 type == "JMX.mbean.registered"（== MBeanServerNotification.REGISTRATION_NOTIFICATION）的 MBeanServerNotification，getMBeanName 等于注册名，handback 原样回传，source 非 null；注销 → "JMX.mbean.unregistered"（== UNREGISTRATION_NOTIFICATION）；removeNotificationListener(delegate,l) 后再注册/注销不再收到。全部等待为有界轮询（≤5s/20ms 步）。

### 版本差异
- 无（delegate 常量与投递行为双侧一致）。
