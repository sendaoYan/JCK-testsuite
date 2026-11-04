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
