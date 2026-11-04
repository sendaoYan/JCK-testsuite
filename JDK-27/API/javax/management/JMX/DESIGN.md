# javax.management.JMX 黑盒行为测试设计

## 1. API 功能
- 常量：`DEFAULT_VALUE_FIELD`、`IMMUTABLE_INFO_FIELD`、`INTERFACE_CLASS_NAME_FIELD`、`LEGAL_VALUES_FIELD`、`MAX_VALUE_FIELD`、`MIN_VALUE_FIELD`、`MXBEAN_FIELD`、`OPEN_TYPE_FIELD`、`ORIGINAL_TYPE_FIELD`。
- 静态方法：`isMXBeanInterface(Class)`、`newMBeanProxy(MBeanServerConnection, ObjectName, Class)`、`newMXBeanProxy(MBeanServerConnection, ObjectName, Class, boolean)`、`getMBeanServerId(MBeanServerConnection)`。

## 2. Java 用户如何使用
创建 MBean/MXBean 的本地代理，或查询 MBeanServer 标识。
```java
ThingMBean proxy = JMX.newMBeanProxy(mbsc, on, ThingMBean.class);
String id = JMX.getMBeanServerId(mbsc);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/proxy/JMXProxyTest.java`：newMBeanProxy 代理调用。
- `test/jdk/javax/management/mxbean/MXBeanInteropTest1.java`：MXBean 判定。
- 实现权威：`src/java.management/share/classes/javax/management/JMX.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：isMXBeanInterface 对标准 MBean 接口为 false、对 MXBean 接口为 true；newMBeanProxy 生成代理并转发 getter；getMBeanServerId 返回非空、与 MBeanServerDelegate MBeanServerId 一致；描述符常量字符串非空。
- 白盒：public 具体类、静态方法、public 常量字段存在。

## 深化（2026-11）：常量全集 + 代理闭环 + MXBean 开类型转换

### 新增覆盖
1. **常量全集**：9 个字段常量精确（defaultValue/immutableInfo/interfaceClassName/legalValues/maxValue/minValue/mxbean/openType/originalType）。
2. **isMXBeanInterface 规则**：@MXBean 注解 true；*MXBean 命名 true；*MBean false；无约定普通接口（java.util.List）false；非接口类 false。
3. **newMBeanProxy 闭环**（本地 server）：读写/操作直达；notificationBroadcast=false 同行为；**4 参 true 的代理 implements NotificationEmitter 且订阅真闭环**（经代理 addNotificationListener → 代理调 emit → 有界轮询收到 type/message/handback 精确）。
4. **newMXBeanProxy + 开类型**：注册 *MXBean 命名 MBean，descriptor `mxbean="true"`（字符串值）；**List&lt;String&gt; 属性经 server.getAttribute 是 String[]、经代理还原 List**（MXBean 开类型转换的双视角锁定）；代理 setter 落到 server；4 参 newMXBeanProxy 可用。

### 版本差异
- `JMX.isNotificationBroadcaster` 不存在（双侧 javap 均无此方法）——不测。

### 参考来源
- Oracle javadoc: JMX、MXBean mapping 规范章节。
- OpenJDK `test/jdk/javax/management/proxy/`（代理思路）。
