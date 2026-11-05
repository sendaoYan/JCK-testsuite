# javax.management.monitor.GaugeMonitorMBean — 设计案

## 1. API 功能
- 接口，定义 GaugeMonitor 的管理接口。
- 方法含高低阈值、通知开关、差异模式的 getter/setter，以及继承自 MonitorMBean 的观测对象/属性管理。

## 2. Java 用户如何使用
通过接口引用配置 GaugeMonitor。
```java
GaugeMonitorMBean gm = new GaugeMonitor();
gm.setThresholds(100, 10);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/monitor/GaugeMonitorMBeanTest.java` 通过接口覆盖状态往返。
- 说明：接口为契约，测试聚焦方法存在性与接口级状态读写。

## 4. 测试用例设计
- 白盒：接口、继承 MonitorMBean、方法含 setThresholds/getHighThreshold/getLowThreshold/setNotifyHigh/setNotifyLow/setDifferenceMode 等。
- 黑盒：用 GaugeMonitor 实例通过接口设置并读取高低阈值、通知标志、差异模式。

## 深化（2026-11 批 I：monitor/timer 真闭环）

### 新增覆盖维度
- **方法集精确对照**：`GaugeMonitorMBean.class.getMethods()` 的 name/参数个数全集（26 个：MonitorMBean 全部 13 + setThresholds/getHighThreshold/getLowThreshold、get/setNotifyHigh、get/setNotifyLow、get/setDifferenceMode、getDerivedGauge×2、getDerivedGaugeTimeStamp×2）。
- **经接口真闭环**：接口引用完成 setThresholds(3,8) → addObservedObject → start → 真实 high/low 通知按序送达 → stop；通知常量串与类常量逐字节相等。
