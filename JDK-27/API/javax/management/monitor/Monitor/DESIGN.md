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
