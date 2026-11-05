# javax.management.monitor.CounterMonitorMBean — 设计案

## 1. API 功能
- 接口，定义 CounterMonitor 的管理接口。
- 方法含初始阈值/偏移/模数/通知开关/差异模式的 getter/setter，以及继承自 MonitorMBean 的观测对象/属性管理与派生量查询（含按 ObjectName 重载）。

## 2. Java 用户如何使用
通过接口引用配置 CounterMonitor。
```java
CounterMonitorMBean cm = new CounterMonitor();
cm.setInitThreshold(5);
cm.setOffset(2);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/monitor/CounterMonitorMBeanTest.java` 通过接口覆盖状态往返（来源：`~/git/jdk8u-dev/test/jdk`）。
- 说明：接口为契约，测试聚焦方法存在性与接口级状态读写。

## 4. 测试用例设计
- 白盒：接口、继承 MonitorMBean、方法含 get/setInitThreshold、get/setOffset、get/setModulus、get/setNotify、get/setDifferenceMode、getDerivedGauge×2、getThreshold×2、setThreshold。
- 黑盒：用 CounterMonitor 实例通过接口设置并读取各属性、观测对象/属性/粒度。

## 深化（2026-11 批 I：monitor/timer 真闭环）

### 新增覆盖维度
- **方法集精确对照**：`CounterMonitorMBean.class.getMethods()` 的 name/参数个数全集（30 个：MonitorMBean 全部 13 + get/setInitThreshold、get/setOffset、get/setModulus、get/setNotify、get/setDifferenceMode、getDerivedGauge×2、getDerivedGaugeTimeStamp×2、getThreshold×2、setThreshold）。
- **经接口真闭环**：`CounterMonitorMBean ref = new CounterMonitor()` 注册进 server 后经接口完成全套配置（initThreshold/offset/modulus/notify/differenceMode）→ start → 真实阈值通知送达 → per-object getThreshold(observedName)/getDerivedGauge(observedName) 回读 → stop。
- **废弃语义区分**：`ref.setThreshold(9)` 与 setInitThreshold 同步；无参 `ref.getThreshold()` 无观察对象时返回 **null**（它报告的是首个观察对象，不是 initThreshold）。

### 实测锁定（JDK-8 与 JDK-27 完全一致）
- 接口方法面、废弃方法行为双侧相同。
