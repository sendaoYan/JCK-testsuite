# javax.management.monitor.StringMonitorMBean — 设计案

## 1. API 功能
- 接口，定义 StringMonitor 的管理接口。
- 方法含比较串、匹配/差异通知开关的 getter/setter，以及继承自 MonitorMBean 的观测对象/属性管理。

## 2. Java 用户如何使用
通过接口引用配置 StringMonitor。
```java
StringMonitorMBean sm = new StringMonitor();
sm.setStringToCompare("hello");
sm.setObservedObject(name);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/monitor/StringMonitorMBeanTest.java` 通过接口覆盖状态往返。
- 说明：接口为契约，测试聚焦方法存在性与接口级状态读写。

## 4. 测试用例设计
- 白盒：接口、继承 MonitorMBean、方法含 getStringToCompare/setStringToCompare/getNotifyMatch/setNotifyMatch/getNotifyDiffer/setNotifyDiffer 等。
- 黑盒：用 StringMonitor 实例通过接口设置并读取比较串、通知标志、观测对象/属性。
