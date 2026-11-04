# javax.management.monitor.MonitorMBean — 设计案

## 1. API 功能
- 接口，定义所有监视器的公共管理接口。
- 方法含 start/stop、观测对象集合管理、观测属性、采样粒度、isActive。
- 部分方法已废弃（getObservedObject()/setObservedObject() 等），但契约仍存在。

## 2. Java 用户如何使用
通过接口引用统一操作任意 Monitor 子类。
```java
MonitorMBean m = new CounterMonitor();
m.setObservedObject(name);
m.setObservedAttribute("Value");
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/monitor/MonitorMBeanTest.java` 通过接口引用覆盖观测对象/属性/粒度。
- 说明：接口为公共契约，测试聚焦方法存在性与接口级状态读写。

## 4. 测试用例设计
- 白盒：接口、方法含 start/stop/addObservedObject/removeObservedObject/containsObservedObject/getObservedObjects/getObservedObject/setObservedObject/getObservedAttribute/setObservedAttribute/getGranularityPeriod/setGranularityPeriod/isActive。
- 黑盒：用 CounterMonitor 实例通过接口设置并读取观测对象/属性/粒度，验证 start 前 isActive 为 false。
