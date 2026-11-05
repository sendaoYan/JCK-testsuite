# javax.management.relation.MBeanServerNotificationFilter — 设计案

## 1. API 功能
- 继承 `NotificationFilterSupport`，用于过滤 `MBeanServerNotification`。
- 构造器启用 `REGISTRATION_NOTIFICATION` 与 `UNREGISTRATION_NOTIFICATION` 两类通知。
- 选择逻辑：`enableObjectName`/`disableObjectName`/`enableAllObjectNames`/`disableAllObjectNames`。
- 查询：`getEnabledObjectNames()`、`getDisabledObjectNames()`、`getEnabledTypes()`（继承）。
- 判定：`isNotificationEnabled(Notification)` 组合类型匹配与 ObjectName 选择。

## 2. Java 用户如何使用
典型组合链路：
```java
MBeanServerNotificationFilter f = new MBeanServerNotificationFilter();
f.disableAllObjectNames();
f.enableObjectName(watched);
if (f.isNotificationEnabled(n)) { /* 转发 */ }
```

## 3. 开源代码实际用法调研
- JDK 源码自用：`javax.management.relation.RelationService` 内部使用 `MBeanServerNotificationFilter myUnregNtfFilter`，`enableObjectName` 注册对引用 MBean 的监听（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation/RelationService.java`）。
- 说明：该类主要由 JMX 关系服务内部使用，开源生态偶见于对 MBeanServer 注册/注销事件的自定义监听过滤。

## 4. 测试用例设计
- 白盒：public 类、父类 NotificationFilterSupport、七个关键方法存在。
- 黑盒：构造默认启用两类通知、`enableAll`/`disableAll` 对 ObjectName 集与 `isNotificationEnabled` 的影响、`enableObjectName`/`disableObjectName` 组合、非 MBeanServer 类型拒绝、三处 null 参数精确 message。

## 深化（2026-11 批 K：空 SELECT 模式 / 拷贝语义 / 类型层叠加 / 序列化 / 真实 delegate 闭环）

- **新建过滤器即空 SELECT 模式：拒绝一切 MBeanServerNotification**（getEnabledObjectNames 空、getDisabledObjectNames null）。
- SELECT/DESELECT 模式内互逆操作：SELECT 内 disable=移出选中集；DESELECT 内 enable=移出取消集；重复 enable/disable 幂等（单条目）。
- getter 防御拷贝：clear 返回 Vector 后内部集不变；非当前模式的 getter 为 null。
- 类型层叠加：继承的 disableType(REGISTRATION) → 注册拒、注销过；两类型全关即使 enableAllObjectNames 也全拒。
- 序列化 round-trip：保留 SELECT/DESELECT 模式与两个集合；SUID=2605900539589789736（ObjectStreamClass 锁定）。
- **真实闭环**：监听 JMImplementation:type=MBeanServerDelegate + 本过滤器（仅 enable A）：注册 A 收 REGISTRATION（type/getMBeanName 精确，有界轮询 ≤5s/50ms）；注册 B 在 500ms 观察窗内不产生任何投递。
