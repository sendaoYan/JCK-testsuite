# javax.management.monitor.MonitorNotification — 设计案

## 1. API 功能
- 具体类，继承 `Notification`。
- 常量：OBSERVED_OBJECT_ERROR、OBSERVED_ATTRIBUTE_ERROR、OBSERVED_ATTRIBUTE_TYPE_ERROR、THRESHOLD_ERROR、RUNTIME_ERROR、THRESHOLD_VALUE_EXCEEDED、THRESHOLD_HIGH_VALUE_EXCEEDED、THRESHOLD_LOW_VALUE_EXCEEDED、STRING_TO_COMPARE_VALUE_MATCHED、STRING_TO_COMPARE_VALUE_DIFFERED。
- 查询方法：`getObservedObject()`、`getObservedAttribute()`、`getDerivedGauge()`、`getTrigger()`（均返回通知快照）。
- 构造器为包私有，不能直接 new，须由监视器生成。

## 2. Java 用户如何使用
作为通知监听器收到的对象类型读取观测快照。
```java
MonitorNotification mn = (MonitorNotification) n;
ObjectName obs = mn.getObservedObject();
Number gauge = (Number) mn.getDerivedGauge();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/monitor/MonitorNotificationTest.java` 通过真实 CounterMonitor 触发通知来覆盖 getter（来源：`~/git/jdk8u-dev/test/jdk`）。
- 说明：构造器不可直接访问（模块不开放），黑盒测试必须通过短生命周期 CounterMonitor 生成真实实例，调度线程为 daemon，jtreg 安全。

## 4. 测试用例设计
- 白盒：继承 Notification、常量字符串校验、方法含 getObservedObject/getObservedAttribute/getDerivedGauge/getTrigger。
- 黑盒：注册动态 CounterBean 与 CounterMonitor，触发 THRESHOLD_VALUE_EXCEEDED，验证 type、observedObject、observedAttribute、derivedGauge、trigger、toString 含类型。

## 深化（2026-11 批 I：monitor/timer 真闭环）

### 新增覆盖维度（真实通知实例断言，非仅常量反射）
- **常量值全表**：10 个 type 常量精确串逐一锁定——`jmx.monitor.error.mbean`/`error.attribute`/`error.type`/`error.threshold`/`error.runtime`、`jmx.monitor.counter.threshold`、`jmx.monitor.gauge.high`/`gauge.low`、`jmx.monitor.string.matches`/`string.differs`。
- **计数器通知 getter 闭环**：真实触发 `THRESHOLD_VALUE_EXCEEDED`，断言 getObservedObject==观测 ObjectName、getObservedAttribute、getDerivedGauge==触发时观测值、getTrigger==initThreshold。
- **错误通知 getter**：属性错误通知的 getDerivedGauge/getTrigger 均 null、getObservedObject/getObservedAttribute 正确回填。
- **白盒契约**：继承 Notification；构造器包私有（getDeclaredConstructors 无 public 项，外部不可 new，只能由监视器产生）；非可复制语义照 Notification。

### 实测锁定（JDK-8 与 JDK-27 完全一致）
- 序列化 svuid 双侧一致：`-4608189663661929204`；十个常量串双侧逐字节相同。
