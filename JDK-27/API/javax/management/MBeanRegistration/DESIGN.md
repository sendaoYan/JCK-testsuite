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

## 深化（2026-11，批次 e）
新增维度：注册→注销全生命周期序、preRegister 改名/返回 null、钩子抛异常矩阵、名冲突 postRegister(false)。
- 全序实测：[preRegister:<canonicalName>, postRegister:true, preDeregister, postDeregister]，preRegister 收到的 server 即目标 server。
- preRegister 返回新 ObjectName → 注册在新名（ObjectInstance 返回新名、旧名未注册、新名 getMBeanInfo 可达）。
- preRegister 返回 null → 注册在传入名（实测两侧一致）。
- 钩子异常矩阵（两侧一致）：
  - preRegister 抛受检 Exception → registerMBean 抛 **MBeanRegistrationException**，**postRegister(false) 不被调用**（log 仅 preRegister），未注册；
  - preRegister 抛 RuntimeException → **RuntimeMBeanException**，未注册；
  - preDeregister 抛受检 → unregisterMBean 抛 **MBeanRegistrationException**，MBean **仍注册**，postDeregister 不被调用；
  - 名冲突 → InstanceAlreadyExistsException，第二个实例仍收到 postRegister(false)（log=[preRegister, postRegister:false]）。
- **嵌套类命名约定易错点**：实现类 RBase 的管理接口必须叫 RBaseMBean（类名+MBean），用别的接口名（哪怕以 MBean 结尾）注册即 NotCompliantMBeanException——首批探针即踩中。
### 版本差异
- 无（生命周期顺序、异常包装两侧一致）。
