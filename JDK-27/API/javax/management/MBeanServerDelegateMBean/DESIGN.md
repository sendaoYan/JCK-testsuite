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

## 深化（2026-11）：接口形状 + 三种到达路径

### 新增覆盖
1. **接口形状精确**：恰 7 个 getter，全部无参、返回 String、无 checked 异常；MBeanServerDelegate 实现之。
2. **三路径同值**：接口引用直调；注册为 MBean 后 server.getAttribute 七属性全非空且内省属性名集合==接口 getter 集；MBeanServerInvocationHandler 代理（以本接口为类型）getSpecificationName/getMBeanServerId 路由到真实 delegate。

### 参考来源
- Oracle javadoc: MBeanServerDelegateMBean。
