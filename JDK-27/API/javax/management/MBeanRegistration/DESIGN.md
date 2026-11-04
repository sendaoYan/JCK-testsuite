# javax.management.MBeanRegistration 黑盒行为测试设计

## 1. API 功能
- 接口：`preRegister(MBeanServer, ObjectName)`、`postRegister(Boolean)`、`preDeregister()`、`postDeregister()`。

## 2. Java 用户如何使用
MBean 实现该接口以参与注册生命周期：注册时决定/改写 ObjectName，注销时抛异常阻止注销。
```java
public ObjectName preRegister(MBeanServer server, ObjectName name) { return name; }
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanServer/PreRegisterTest.java`、`PreDeregisterDeadlockTest.java`。
- `test/jdk/javax/management/mxbean/MXBeanPreRegisterTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanRegistration.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：把实现 MBeanRegistration 的对象直接注册到平台服务器，验证 preRegister 收到服务器与 ObjectName、postRegister 收到 true、preDeregister/postDeregister 在注销时按序触发；finally 注销。
- 白盒：public 接口、含四个回调方法。
