# javax.management.monitor.GaugeMonitor — 设计案

## 1. API 功能
- 具体类，继承 `Monitor` 并实现 `GaugeMonitorMBean`。
- 核心属性：`HighThreshold`/`LowThreshold`（`setThresholds`）、`NotifyHigh`/`NotifyLow`、`DifferenceMode`、`DerivedGauge`。
- 超出高阈值发 `THRESHOLD_HIGH_VALUE_EXCEEDED`，低于低阈值发 `THRESHOLD_LOW_VALUE_EXCEEDED`。
- 部分方法已废弃（getHighThreshold/getLowThreshold/setThresholds(Number,Number) 等）。

## 2. Java 用户如何使用
监控一个在高低阈值间波动的量。
```java
GaugeMonitor gm = new GaugeMonitor();
gm.setThresholds(100, 10);
gm.setObservedObject(name);
gm.setObservedAttribute("Level");
gm.start();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/monitor/GaugeMonitorTest.java` 覆盖高低阈值、通知与差异模式。
- 说明：阈值须同类型且高≥低，否则抛 IllegalArgumentException；测试聚焦无启动线程的状态与校验。

## 4. 测试用例设计
- 白盒：非接口、继承 Monitor、实现 GaugeMonitorMBean。
- 黑盒：默认阈值/通知/差异模式/派生量；setThresholds 高/低往返；setNotifyHigh/Low、setDifferenceMode；observed object/attribute/granularity；通知信息含高/低阈值类型；异常路径（null 阈值、高<低、跨类型）。

## 深化（2026-11 批 I：monitor/timer 真闭环）

### 新增覆盖维度（本地 MBeanServer 真调度）
- **双阈值迟滞闭环**：low=3 high=8，观测序列 5→9→5→2→9：升穿 9 → 恰一次 `jmx.monitor.gauge.high`（trigger==high）；回落 5（仍高于 low）不再通知；跌破 2 → 恰一次 `jmx.monitor.gauge.low`（trigger==low）；再次升穿 → 再一次 high（迟滞语义：每个方向需反向穿越后才重新武装）。
- **开关矩阵**：notifyHigh/notifyLow 四组合，关掉的方向只更新派生量不发通知。
- **校验消息**：setThresholds(null,·)/(·,null)→IAE "Null threshold value"；high<low→IAE "High threshold less than low threshold"；失败调用后双阈值保持 **Integer 0 默认**（非 null）。
- **差值模式瞬态**：5.0→8.0 差值 3.0 只存活一个轮询窗口（25ms 细粒度轮询捕获），随后归 0。
- **类型错**：观测 String 属性 → `jmx.monitor.error.type` 真实送达。
- **默认值与未知对象**：新实例 getHighThreshold/getLowThreshold 为 **Integer 0 非 null**、派生量 Integer 0；未观察对象 → getDerivedGauge(unknown) null、getDerivedGaugeTimeStamp(unknown) 0。

### 实测锁定（JDK-8 与 JDK-27 完全一致）
- 阈值默认值是 Integer 0 而非 null（易错点：容易想当然断言 null）。
