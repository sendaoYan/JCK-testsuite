# javax.management.modelmbean.ModelMBean — 设计案

## 1. API 功能
- 接口，聚合 `DynamicMBean`、`PersistentMBean`、`ModelMBeanNotificationBroadcaster` 的能力。
- 核心方法：`setModelMBeanInfo`/`getMBeanInfo`、`setManagedResource(Object)`/`setManagedResource(Object, String)`/`getManagedResource`、`invoke` 等。

## 2. Java 用户如何使用
实现该接口（通常用 `RequiredModelMBean`）暴露一个由 `ModelMBeanInfo` 描述的受管资源，实现动态属性/操作/通知。
```java
ModelMBean mb = new RequiredModelMBean(info);
mb.setManagedResource(resource, "ObjectReference");
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/modelmbean/` 系列（如 RequiredModelMBeanTest）通过实现或代理覆盖接口。
- 说明：接口是标准 MBean 到“模型驱动”动态 MBean 的桥梁，测试聚焦接口继承关系与方法存在性。

## 4. 测试用例设计
- 白盒：接口、继承 `DynamicMBean`/`PersistentMBean`/`ModelMBeanNotificationBroadcaster`、方法含 `setModelMBeanInfo`/`setManagedResource`/`getManagedResource`。
- 黑盒：匿名实现或用 RequiredModelMBean 验证最小组合契约（如 setModelMBeanInfo 反映到 getMBeanInfo）。
