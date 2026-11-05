# javax.management.timer.Timer — 设计案

## 1. API 功能
- 具体类，继承 `NotificationBroadcasterSupport` 并实现 `TimerMBean`、`MBeanRegistration`。
- 核心：`addNotification` 三个重载（一次性/周期/固定速率）；`removeNotification`/`removeNotifications`/`removeAllNotifications`；`getAllNotificationIDs`/`getNotificationIDs(type)`；逐通知查询 type/message/userData/date/period/nbOccurences/fixedRate；`isActive`/`isEmpty`；`setSendPastNotifications`。
- 常量：`ONE_SECOND`/`ONE_MINUTE`/`ONE_HOUR`/`ONE_DAY`/`ONE_WEEK`。

## 2. Java 用户如何使用
注册定时器后添加通知，指定触发时间与周期。
```java
Timer t = new Timer();
Integer id = t.addNotification("type", "msg", "user", new Date(futureMillis), 1000L, 5L);
t.start();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/timer/TimerTest.java` 覆盖添加/查询/移除与校验。
- 说明：`addNotification` 会把过去的时间钳制为 now，故测试用未来固定毫秒值（4102444800000L）保证确定性；默认 `sendPastNotifications=false`。

## 4. 测试用例设计
- 白盒：非接口、继承 NotificationBroadcasterSupport、实现 TimerMBean/MBeanRegistration。
- 黑盒：初始空/inactive/零通知/默认不补发；多类型通知添加后 ID 唯一；按类型与全部检索；逐通知查询（周期/次数/固定速率）；removeNotification/removeNotifications/removeAllNotifications 及二次移除抛 InstanceNotFoundException；setSendPastNotifications 往返；null 日期/负周期抛 IllegalArgumentException；常量校验。

## 深化（2026-11 批 I：monitor/timer 真闭环）

### 新增覆盖维度（真实调度，短周期 100-200ms，有界等待）
- **一次性**：未来 300ms 单发 → 恰一次送达后通知**自动删除**（isEmpty() 真）。
- **周期无限**：period 200ms 无 nb 次数 → 至少 4 次连续送达、seqNumber 严格递增；getNbOccurences==0。
- **有限次数**：nbOccurrences=3 → 恰 3 次后自动删除（等待 isEmpty 有界，再 remove → InstanceNotFoundException）。
- **past 语义三态**：
  - 未激活时加过去日期：日期**钳制到当前**，且 addNotification **就地改写调用方传入的 Date 对象**（先存 pastMs 再 add 才能断言 getDate(id)>pastMs）；
  - sendPastNotifications=false：start 时不补发已过期项（静默窗零通知）；
  - =true：start 即补发一次。
  - 激活中加过去日期：立即送达。
- **stop→start 补发**：周期通知 stop 期间到期的部分在 start 后补发（pending 不丢失）。
- **删除与 id/seq 重置**：removeNotification 后 getDate(id) null；removeAllNotifications 清空且**新通知 id 重新从 1 起**；同一 Timer 的 seqNumber 首条为 1（跨 removeAll 不复位 seq）。
- **经 server 属性面**：Timer 注册为 MBean，经 MBeanServer.setAttribute/getAttribute 操作 SendPastNotifications/GranularityPeriod 等属性 + 经 ObjectName invoke addNotification 真投递。

### 实测锁定（JDK-8 与 JDK-27 完全一致）
- TimerAlarmClock 为 daemon 调度；发送后的一次性通知自动移除与 removeNotification 存在竞态——断言删除路径前先有界等待 isEmpty。
- addNotification 修改传入 Date 是双侧一致的既定行为（规范未明说，实测锁定）。
