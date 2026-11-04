# javax.management.MBeanServerConnection 黑盒行为测试设计

## 1. API 功能
- 接口：`getAttribute/setAttribute/getAttributes/setAttributes`、`invoke`、`queryMBeans/queryNames`、`getMBeanInfo/getObjectInstance/isRegistered/isInstanceOf/getMBeanCount/getDefaultDomain` 等。

## 2. Java 用户如何使用
连接本地或远程 MBeanServer 的抽象（MBeanServer 继承它）；JMX 代理也基于它。
```java
Object v = mbsc.getAttribute(on, "Count");
Set<ObjectName> names = mbsc.queryNames(new ObjectName("d:*"), null);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/proxy/JMXProxyTest.java`：基于连接创建代理。
- `test/jdk/javax/management/MBeanServer/MBeanServerNotificationTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanServerConnection.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：用平台服务器（MBeanServer 实现该接口）验证 getAttribute/setAttribute/getAttributes/invoke/getMBeanInfo/queryNames/isInstanceOf/getMBeanCount 的行为。
- 白盒：public 接口、含 getAttribute/invoke/queryMBeans/getMBeanInfo 等方法。

## 深化（2026-11）：接口形状 + 本地全方法可达

### 新增覆盖
1. **方法名集合精确锁定**：接口公开方法名恰为 18 个（addNotificationListener/createMBean×4/getAttribute(s)/setAttribute(s)/getDefaultDomain/getDomains/getMBeanCount/getMBeanInfo/getObjectInstance/invoke/isInstanceOf/isRegistered/queryMBeans/queryNames/removeNotificationListener×4/unregisterMBean），两侧一致（javap 双侧对照 SAME）。
2. **远程契约**：接口每个方法都声明 `java.io.IOException`。
3. **is-a**：`MBeanServer is-a MBeanServerConnection`。
4. **行为闭环**（本地 newMBeanServer 以 Connection 类型引用）：createMBean 注册、属性读写、批量、invoke 带签名、查询、域/计数（==2 精确）、getMBeanInfo/getObjectInstance/isInstanceOf、delegate 通知 add/remove 真收注册通知（有界轮询 ≤5s/50ms）、注销；负例 INFE（未注册名）与 ANFE（未知属性，注册仍在时触发——先于注销，顺序敏感）。

### 易错细节
- getAttribute 未知属性在 MBean **已注销**时抛 INFE 而非 ANFE——负例须按注册状态排序。

### 参考来源
- Oracle javadoc: MBeanServerConnection（方法契约与 IOException 声明）。
- OpenJDK `test/jdk/javax/management/proxy/JMXProxyTest.java`（连接类型用法思路）。
