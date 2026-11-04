# javax.management.monitor.CounterMonitorMBean — 设计案

## 1. API 功能
- 接口，定义 CounterMonitor 的管理接口。
- 方法含阈值/偏移/模数/通知/差异模式的 getter/setter，以及继承自 MonitorMBean 的观测对象/属性/粒度管理。

## 2. Java 用户如何使用
通过 MBean 接口以类型安全方式配置 CounterMonitor。
```java
CounterMonitorMBean cm = new CounterMonitor();
cm.setInitThreshold(5);
cm.setObservedObject(name);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/monitor/CounterMonitorMBeanTest.java` 通过接口引用覆盖 getter/setter。
- 说明：接口为行为契约，测试聚焦方法存在性与通过接口引用的状态往返。

## 4. 测试用例设计
- 白盒：接口、继承 MonitorMBean、方法含 setInitThreshold/getInitThreshold/setOffset/getOffset/setModulus/getModulus 等。
- 黑盒：用 CounterMonitor 实例通过接口设置并读取 initThreshold/offset/modulus，验证观测对象与粒度。
