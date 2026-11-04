# javax.management.MBeanServerDelegateMBean 黑盒行为测试设计

## 1. API 功能
- 接口：`getMBeanServerId()`、`getSpecificationName()`、`getSpecificationVersion()`、`getSpecificationVendor()`、`getImplementationName()`、`getImplementationVersion()`、`getImplementationVendor()`。

## 2. Java 用户如何使用
MBeanServerDelegate 暴露给管理客户端读服务器标识与版本信息。
```java
MBeanServerDelegateMBean d = JMX.newMBeanProxy(mbs, delegateName, MBeanServerDelegateMBean.class);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/ImplementationVersion/`：版本字段。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanServerDelegateMBean.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：通过 MBeanServer.getAttribute 在 delegate 上读取全部 7 个属性，验证 spec 名为 "Java Management Extensions"、spec 版本 "1.4"，其余非空/含预期值。
- 白盒：public 接口、含 getMBeanServerId/getSpecificationName/getImplementationVersion 等。
