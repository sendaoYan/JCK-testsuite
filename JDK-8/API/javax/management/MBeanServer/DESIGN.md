# javax.management.MBeanServer 黑盒行为测试设计

## 1. API 功能
- 接口（MBeanServerConnection 的子接口）：注册/注销 MBean，读/写属性，调用操作，查询 MBean，创建/查询 MBean 与类加载器。

## 2. Java 用户如何使用
所有 JMX 操作的核心入口，由 MBeanServerFactory 或平台服务器提供实例。
```java
MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
ObjectInstance oi = mbs.registerMBean(bean, on);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanServer/*Test.java`（注册/注销/异常/通知）。
- `test/jdk/javax/management/MBeanServerFactory/ReleaseMBeanServerTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanServer.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：用平台服务器注册 StandardMBean，验证 isRegistered/registerMBean/getAttribute/setAttribute/invoke/isInstanceOf/unregisterMBean；isInstanceOf 用实现类名（源码确认检查类而非接口）。
- 白盒：public 接口、父接口 MBeanServerConnection、含 registerMBean/unregisterMBean/queryNames 等。

## 深化（2026-11）：本地 newMBeanServer 真闭环

在原有平台服务器用例之上追加 5 个本地闭环方法（每方法独立 `MBeanServerFactory.newMBeanServer()`，域前缀 `jck.srv/jck.q/jck.mi`）：

### 新增覆盖
1. **registerMBean/ObjectInstance**：返回值 name/className 精确；重复注册 `InstanceAlreadyExistsException`；isInstanceOf 精确类与父类名均 true。
2. **属性/操作 round-trip**：isIs 属性 `Ready`；getAttributes 批量（未知名静默省略、map 化取值不依赖顺序）；setAttributes 批量（只返回成功项）；`invoke("add",{5},{"int"})` 精确返回 47（纯操作不改状态）、void 操作返回 null 且状态可见。
3. **异常矩阵（实测锁定）**：
   - `getAttribute` 未知属性 → `AttributeNotFoundException`，消息含属性名（"No such attribute: X"）。
   - `invoke` 未知操作 → `ReflectionException`，getCause 为 `NoSuchMethodException`，且 `getTargetException()==getCause()`，消息含操作名。
   - MBean 方法抛自造异常 → `MBeanException`，getCause/getTargetException 为**同一实例**。
   - setAttribute 错值类型（String 给 int）→ `InvalidAttributeValueException`，消息含属性名（"Invalid value for attribute Count: notAnInt"），**状态不变**。
   - 未注册名：getAttribute/setAttribute/getAttributes/setAttributes/invoke/unregisterMBean/getMBeanInfo/getObjectInstance/isInstanceOf 九操作各一 `InstanceNotFoundException`。
   - null 矩阵：setAttribute(null)/Attribute(null,v)/getAttribute(null attr)/getAttribute(null name)/isRegistered(null)/unregisterMBean(null)/getMBeanInfo(null)/getObjectInstance(null) → `RuntimeOperationsException`（cause IAE）；invoke(null op) → `ReflectionException`。
4. **查询/域/计数/生命周期**：queryNames("jck.q:*") 内容精确、精确名、未知域空集；queryMBeans ObjectInstance 内容；getDomains 含注册域与 JMImplementation；getMBeanCount=delegate+N 精确；getDefaultDomain=="DefaultDomain"；getClassLoaderFor==测试类加载器；注销后 isRegistered false + 后续操作 INFE。
5. **内省元数据 + createMBean**：属性 2 个读写/isIs 矩阵精确（Count rw / Ready r+is，类型串 int/boolean）；操作名集合 {reset,add,boom} 精确；info descriptor `immutableInfo="true"`/`mxbean="false"`（**值为字符串**）；createMBean(className,name) 一气呵成。

### 版本差异（双侧实测）
- `registerMBean(null, name)`：JDK-8 直接 `NullPointerException`；JDK-9+（含 27）`RuntimeOperationsException`(cause IAE)。两侧各自断言真实行为。
- 其余所有行为双侧一致。

### 参考来源
- Oracle javadoc: javax.management.MBeanServer（异常矩阵章节）。
- OpenJDK `test/jdk/javax/management/MBeanServer/MBeanTest.java`、`ExceptionTest.java`、`MBeanExceptionTest.java`（异常传播思路，代码自写）。
