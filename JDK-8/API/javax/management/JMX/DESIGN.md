# javax.management.JMX 契约测试设计（2026-11 新建）

## 1. API 功能
- 工具类：Descriptor 字段名常量、newMBeanProxy/newMXBeanProxy、isMXBeanInterface。

## 2. Java 用户典型用法
见 JDK-27 侧同目录设计与下节深化用例（双侧同代码）。

## 3. 参考来源
- Oracle javadoc: javax.management.JMX。
- OpenJDK `test/jdk/javax/management/` 对应目录（思路借鉴，代码自写）。
## 4. 测试用例设计
见下节深化清单。

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
