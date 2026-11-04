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
