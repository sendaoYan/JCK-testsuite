# javax.management.MBeanServerConnection 契约测试设计（2026-11 新建）

## 1. API 功能
- 接口（MBean 的远程/本地统一访问视图）：createMBean、属性读写、invoke、查询、通知订阅、isInstanceOf/getMBeanCount/getDomains 等只读视图。

## 2. Java 用户典型用法
见 JDK-27 侧同目录设计与下节深化用例（双侧同代码）。

## 3. 参考来源
- Oracle javadoc: javax.management.MBeanServerConnection。
- OpenJDK `test/jdk/javax/management/` 对应目录（思路借鉴，代码自写）。
## 4. 测试用例设计
见下节深化清单。

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
