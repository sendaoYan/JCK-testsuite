# javax.management.monitor.CounterMonitor — 设计案

## 1. API 功能
- 具体类，继承 `Monitor` 并实现 `CounterMonitorMBean`。
- 核心属性：`InitThreshold`、`Offset`、`Modulus`、`Notify`、`DifferenceMode`、`DerivedGauge`（含时间戳）。
- 派生量默认取观测属性数值；差异模式下取相邻采样之差；触发阈值比较后发 `MonitorNotification.THRESHOLD_VALUE_EXCEEDED`。
- 部分方法（`setThreshold`、`getDerivedGauge()` 等）已废弃，但行为仍可验证。

## 2. Java 用户如何使用
注册 CounterMonitor 并指定观测对象与属性，当计数超过阈值时收到通知。
```java
CounterMonitor cm = new CounterMonitor();
cm.setObservedObject(name);
cm.setObservedAttribute("Count");
cm.setInitThreshold(5);
cm.start();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/monitor/CounterMonitorTest.java` 覆盖阈值、偏移、模数、差异模式与通知。
- 说明：阈值类型由运行时观测值类型推导（如 Integer→INTEGER），测试聚焦无启动线程情况下的状态往返与参数校验。

## 4. 测试用例设计
- 白盒：非接口、继承 Monitor、实现 CounterMonitorMBean。
- 黑盒：默认 InitThreshold/Offset/Modulus/Notify/DifferenceMode/DerivedGauge；setInitThreshold/setThreshold 别名、setOffset/setModulus/setNotify/setDifferenceMode 往返；继承的 observed object/attribute/granularity；通知信息含 THRESHOLD_VALUE_EXCEEDED；异常路径（null/负数阈值、null 偏移、负数模数）。

## 深化（2026-11 批 I：monitor/timer 真闭环）

### 新增覆盖维度（本地 MBeanServer 真调度，granularity 100-200ms，有界等待）
- **阈值闭环**：initThreshold=5、观测值 0→10 → 恰一次 `jmx.monitor.counter.threshold`（waitFor 有界 5s）；getDerivedGauge==10、时间戳>0；trigger==5；此后值继续增长不再通知（alreadyNotified）。
- **offset 连环**：threshold=5 offset=4，观测值逐级 6/11/15 → 阈值随之上调 9/13/17，逐次等待逐次断言（getThreshold(observedName) 跟踪上调值）。
- **modulus 回绕（差值模式）**：modulus=10、baseline=9 → 置 0 → 派生量= (0-9+10)=1，绝非 -9；先以派生量时间戳**变化**确认首轮 baseline 已建立再回绕（时间戳在 addObservedObject 时即非零）。
- **非差值模式对照**：derivedGauge 恒等于观测值且稳定（非瞬态）。
- **stop 后不再收**：stop 后改值跨阈值，静默窗内零新通知（记录计数→等待→计数不变模式）。
- **notify=false**：派生量持续更新但永不发阈值通知。
- **阈值类型错**：Integer initThreshold 观测 double 属性 → `jmx.monitor.error.threshold` 真实送达。
- **未知对象 getters**：未观察的 ObjectName → getDerivedGauge/getThreshold 返回 null、getDerivedGaugeTimeStamp 返回 0。
- **参数校验矩阵**：setInitThreshold(null)→IAE "Null threshold"、负数→"Negative threshold"；setOffset(null)→IAE；setModulus(null)→IAE、负数→"Negative modulus"（精确消息）。

### 实测锁定（JDK-8 与 JDK-27 完全一致）
- 差值模式派生量是**瞬态**：只在变化后的一个轮询窗口保持差值，下轮归 0——断言用细粒度轮询（25ms）捕获窗口。
- 废弃 getThreshold()（无参）返回**首个观察对象**的阈值，无观察对象时为 null（与 getInitThreshold 不是一回事）。
