# javax.management.NotificationListener 契约测试设计

## 1. API 功能
- 接口，扩展 java.util.EventListener，仅一个方法：`handleNotification(Notification, Object handback)`（void、无受检异常）。

## 2. Java 用户如何使用
实现该接口接收通知；handback 是注册时透传的任意对象。函数式接口，可直接 lambda。
```java
server.addNotificationListener(name, (n, hb) -> { ... }, null, handback);
```

## 3. 参考来源
- Oracle javadoc：javax.management.NotificationListener。
- OpenJDK `test/jdk/javax/management/MBeanServer/MBeanServerNotificationTest.java`（delegate 注册/注销通知回归思路，代码自写）。
- 实现权威：`src/share/classes/javax/management/NotificationListener.java`、`MBeanServerDelegate.java`。

## 4. 测试用例设计 + 易错规范细节
- 白盒：public 接口、extends EventListener、恰 1 个声明方法且无受检异常。
- 行为：lambda 直接可用（type+handback 拼串断言）。
- 真 server 闭环（`MBeanServerFactory.newMBeanServer()`，不进注册表）：监听 `MBeanServerDelegate.DELEGATE_NAME`；注册自造 Counter（嵌套 CounterMBean 接口，命名满足 Standard MBean 约定：类 Counter + 接口 CounterMBean）→ 收到 type == "JMX.mbean.registered"（同时等于 MBeanServerNotification.REGISTRATION_NOTIFICATION）的 MBeanServerNotification，getMBeanName 等于注册名，handback 原样回传，source 非 null；注销 → "JMX.mbean.unregistered"；removeNotificationListener(delegate, l) 后再注册/注销不再收到。
- 易错点：等待必须用有界轮询（≤5s/20ms 步），不 sleep 长等；delegate 通知实际同步投递，轮询只是防御。

## 深化（2026-11，批次 d）
- 接口形状：恰 1 个声明方法 handleNotification(Notification,Object)，返回 void、无受检异常；extends EventListener；lambda（函数式接口）直接可用。
- 真 server 闭环（MBeanServerFactory.newMBeanServer）：监听 MBeanServerDelegate.DELEGATE_NAME；注册自造 Counter（嵌套 CounterMBean，类名/接口名满足 Standard MBean 配对约定）→ 收到 type == "JMX.mbean.registered"（== MBeanServerNotification.REGISTRATION_NOTIFICATION）的 MBeanServerNotification，getMBeanName 等于注册名，handback 原样回传，source 非 null；注销 → "JMX.mbean.unregistered"（== UNREGISTRATION_NOTIFICATION）；removeNotificationListener(delegate,l) 后再注册/注销不再收到。全部等待为有界轮询（≤5s/20ms 步）。

### 版本差异
- 无（delegate 常量与投递行为双侧一致）。
