# javax.management.monitor.Monitor — 设计案

## 1. API 功能
- 抽象类，继承 `NotificationBroadcasterSupport` 并实现 `MonitorMBean`、`MBeanRegistration`。
- 核心：`start`/`stop` 抽象方法；观测对象集合管理 `addObservedObject`/`removeObservedObject`/`containsObservedObject`/`getObservedObjects`；观测属性 `get/setObservedAttribute`；采样粒度 `get/setGranularityPeriod`；`isActive`。
- 大量方法已废弃（改用按 ObjectName 的重载），但行为仍可验证。

## 2. Java 用户如何使用
子类（CounterMonitor/GaugeMonitor/StringMonitor）继承其调度与观测对象管理；用户一般不直接实例化。
```java
monitor.setObservedObject(name);
monitor.setObservedAttribute("State");
monitor.setGranularityPeriod(500);
monitor.start();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/monitor/MonitorTest.java` 通过匿名子类覆盖观测对象/属性/粒度与校验。
- 说明：测试用不启动线程的匿名子类验证继承状态，保证确定性。

## 4. 测试用例设计
- 白盒：abstract、继承 NotificationBroadcasterSupport、实现 MonitorMBean/MBeanRegistration、方法含 start/stop/观测对象族/观测属性/粒度/isActive。
- 黑盒：匿名子类验证默认值（inactive、无观测对象/属性、粒度 10000ms）；setObservedObject/add/remove/contains/getObservedObjects；观测属性与粒度往返；异常路径（null/0/负粒度）。

## 深化（2026-11 批 I：monitor/timer 真闭环）

### 新增覆盖维度（本地 MBeanServer 真调度，非反射冒烟）
- **生命周期与 MBeanRegistration**：匿名 CounterMonitor 子类记录回调顺序 `preRegister → postRegister(true) → preDeregister → postDeregister`；preRegister 注入的 server 与注册所用 server 同一实例（匿名子类经继承的 protected 字段 `Monitor.server` 读回验证；注意简单名 `server` 在子类体内被继承字段遮蔽，捕获局部须改名）；preDeregister 自动 stop（注销后 `isActive()==false`）；registerMBean 返回 preRegister 的名字。
- **错误通知矩阵（四种 error 通知真实触发 + 精确 type 串）**：
  - 属性不存在 → `jmx.monitor.error.attribute`（通知的 getDerivedGauge/getTrigger 为 null；监控继续轮询 isActive 仍 true）
  - 观测 MBean 未注册 → `jmx.monitor.error.mbean`
  - 类型不支持（CounterMonitor 观测 double 属性）→ `jmx.monitor.error.type`
  - getter 抛 `IllegalStateException("boom")` → `jmx.monitor.error.runtime`，message 携带原因文本
  - 轮询失败从不以异常形式抛出（start() 不抛），全部走通知通道；alreadyNotified 机制保证同一错误只报一次
- **多观测对象**：一个 CounterMonitor 同时观察两个 MBean（`getObservedObjects().length==2`、contains 双真），各自独立派生量，每对象各得一次阈值通知。
- **start/stop 幂等**：二次 start 保持 active、二次 stop 保持 inactive，均无异常；stop 后可重新 start。
- **观测对象集合语义**：`getObservedObjects()` 返回快照（改数组元素不影响内部）；重复 add 同名 no-op；`setObservedObject` 整组替换；废弃 `getObservedObject()` 返回首个；`removeObservedObject(null)` 静默 no-op。

### 实测锁定（JDK-8 与 JDK-27 完全一致）
- `addObservedObject()` 即刻把派生量置 0 并打时间戳（首轮采样前时间戳已非 0）——等待首轮 baseline 必须以时间戳**变化**为信号，而非等非零。
