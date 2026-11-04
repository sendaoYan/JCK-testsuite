# javax.management.monitor.MonitorNotification — 设计案

## 1. API 功能
- 具体类，继承 `Notification`。
- 常量：OBSERVED_OBJECT_ERROR、OBSERVED_ATTRIBUTE_ERROR、OBSERVED_ATTRIBUTE_TYPE_ERROR、THRESHOLD_ERROR、RUNTIME_ERROR、THRESHOLD_VALUE_EXCEEDED、THRESHOLD_HIGH_VALUE_EXCEEDED、THRESHOLD_LOW_VALUE_EXCEEDED、STRING_TO_COMPARE_VALUE_MATCHED、STRING_TO_COMPARE_VALUE_DIFFERED。
- 查询方法：`getObservedObject()`、`getObservedAttribute()`、`getDerivedGauge()`、`getTrigger()`（均返回通知快照）。
- 构造器为包私有，不能直接 new，须由监视器生成。

## 2. Java 用户如何使用
作为通知监听器收到的对象类型读取观测快照。
```java
if (n instanceof MonitorNotification mn) {
    ObjectName obs = mn.getObservedObject();
    Number gauge = (Number) mn.getDerivedGauge();
}
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/monitor/MonitorNotificationTest.java` 通过真实 CounterMonitor 触发通知来覆盖 getter。
- 说明：构造器不可直接访问（模块不开放），黑盒测试必须通过短生命周期 CounterMonitor 生成真实实例，调度线程为 daemon，jtreg 安全。

## 4. 测试用例设计
- 白盒：继承 Notification、常量字符串校验、方法含 getObservedObject/getObservedAttribute/getDerivedGauge/getTrigger。
- 黑盒：注册动态 CounterBean 与 CounterMonitor，触发 THRESHOLD_VALUE_EXCEEDED，验证 type、observedObject、observedAttribute、derivedGauge=10、trigger=5、toString 含类型。
