# javax.management.MBeanServerNotification 契约测试设计（2026-11 新建）

## 1. API 功能
- server 级注册/注销通知值对象（Notification 子类）：常量串、getMBeanName、构造与过滤语义。

## 2. Java 用户典型用法
见 JDK-27 侧同目录设计与下节深化用例（双侧同代码）。

## 3. 参考来源
- Oracle javadoc: javax.management.MBeanServerNotification。
- OpenJDK `test/jdk/javax/management/` 对应目录（思路借鉴，代码自写）。
## 4. 测试用例设计
见下节深化清单。

## 深化（2026-11）：构造契约 + 真注册/注销通知闭环

### 新增覆盖
1. **构造 getter**：常量串全等（"JMX.mbean.registered"/"JMX.mbean.unregistered"）；type/mbeanName/seq/source 精确回读；toString 含 type 与 mbean 名；setUserData round-trip；**getMessage() 为空串 "" 而非 null（实测）**；null ObjectName 构造容忍（getMBeanName()==null）。
2. **真闭环**：监听本地 server 的 delegate → register+unregister 一个 MBean → 有界轮询（≤5s/50ms）恰收 2 条：先 REGISTRATION 后 UNREGISTRATION、getMBeanName 精确、**getSource()==DELEGATE_NAME**、序列号严格递增、handback 同一实例两次。
3. **过滤器分支**：只放行 UNREGISTRATION 的 filter → 注册被抑制、恰收 1 条注销通知。

### 参考来源
- Oracle javadoc: MBeanServerNotification（source 语义）。
- OpenJDK `test/jdk/javax/management/MBeanServer/MBeanServerNotificationTest.java`、`NewMBeanListenerTest.java`（思路）。
