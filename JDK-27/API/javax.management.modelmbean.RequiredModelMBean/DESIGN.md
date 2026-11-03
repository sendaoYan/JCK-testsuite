# javax.management.modelmbean.RequiredModelMBean — 设计案

## 1. API 功能
- 具体类，实现 `ModelMBean`、`MBeanRegistration`、`NotificationEmitter`。
- 无参构造器生成默认 `ModelMBeanInfo`；`RequiredModelMBean(ModelMBeanInfo)` 用给定元数据初始化。
- `setModelMBeanInfo` 更新元数据；`getMBeanInfo` 返回当前元数据；属性/操作按 ModelMBeanInfo 动态解析。

## 2. Java 用户如何使用
作为标准“模型驱动”动态 MBean 实现，绑定元数据与受管资源后注册到 MBeanServer。
```java
RequiredModelMBean mb = new RequiredModelMBean(info);
mb.setManagedResource(resource, "ObjectReference");
ObjectInstance oi = mbs.registerMBean(mb, new ObjectName("x:type=T"));
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/modelmbean/RequiredModelMBeanTest.java` 覆盖注册/取消注册与属性访问。
- 说明：无属性时 `getAttribute` 抛 `AttributeNotFoundException`；`registerMBean` 返回 `ObjectInstance`。

## 4. 测试用例设计
- 白盒：非接口、实现 ModelMBean/MBeanRegistration/NotificationEmitter、无参 + ModelMBeanInfo 构造器。
- 黑盒：无参 getMBeanInfo 非空；setModelMBeanInfo 反映到 getMBeanInfo；无属性 getAttribute 抛 AttributeNotFoundException；私有 MBeanServer 注册/取消注册生命周期（registerMBean 返回 ObjectInstance、isRegistered 变化）。
