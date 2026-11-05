# javax.management.relation.MBeanServerNotificationFilter — 设计案

## 1. API 功能
- extends NotificationFilterSupport；过滤 MBeanServerNotification（注册/注销通知）。
- 两层过滤：①继承的类型层（构造器默认启用 REGISTRATION/UNREGISTRATION 两类型，可经继承的 disableType 调整）；②ObjectName 选择层（两种互斥模式）：
  - SELECT 模式（selectedNames 非 null）：仅选中名通过；**新建过滤器即此模式且选中集为空 → 拒绝一切**。
  - DESELECT 模式（enableAllObjectNames 后 selectedNames==null）：除显式 disable 的名外全部通过。
  - disableAllObjectNames：回到空 SELECT 模式，全拒绝。
- 查询：`getEnabledObjectNames()`/`getDisabledObjectNames()`（**返回防御拷贝 Vector；模式对应的另一侧为 null**）。

## 2. Java 用户如何使用
```java
MBeanServerNotificationFilter f = new MBeanServerNotificationFilter();
f.disableAllObjectNames();
f.enableObjectName(watched);
server.addNotificationListener(delegate, listener, f, null);
```

## 3. 参考来源
- Oracle javadoc：javax.management.relation.MBeanServerNotificationFilter（SELECT/DESELECT 语义、null getter 约定）。
- OpenJDK 源码：`~/git/jdk8u-dev` `src/share/classes/javax/management/relation/MBeanServerNotificationFilter.java`（模式切换与拷贝实现），只借鉴思路、代码自写。

## 4. 测试用例设计（易错规范细节）
- **新建过滤器拒绝一切 MBeanServerNotification**（空 SELECT 模式）——最易错点。
- SELECT 内 disable=移出选中集；DESELECT 内 enable=移出取消集；同名重复 enable/disable 幂等（单条目）。
- getter 返回拷贝：clear 返回的 Vector 后内部集不变；非当前模式的 getter 为 null（enableAll → enabled null、disableAll → disabled null）。
- 类型层与选择层叠加：disableType(REGISTRATION) 后注册通知被拒、注销仍过；两类型全关则全拒（即使 enableAllObjectNames）。
- isNotificationEnabled：null → IAE "Invalid parameter."；非 MBeanServerNotification → false（即使类型串相同）。
- 序列化：round-trip 保留 SELECT/DESELECT 模式与两个集合；SUID=2605900539589789736（ObjectStreamClass 锁定）。
- **真实闭环**：监听 JMImplementation:type=MBeanServerDelegate + 本过滤器（仅 enable A）：注册 A → 收到 REGISTRATION（type/getMBeanName 精确）；注册 B → 有界观察窗（500ms）内不收任何通知。
