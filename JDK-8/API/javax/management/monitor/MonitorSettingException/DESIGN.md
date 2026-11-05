# javax.management.monitor.MonitorSettingException — 设计案

## 1. API 功能
- 单一异常类，继承 `javax.management.JMRuntimeException`（再上为 RuntimeException）。
- 构造器：无参 + `(String message)`，用于表示监视器配置非法。

## 2. Java 用户如何使用
监视器设置非法参数时抛出/捕获。
```java
try { monitor.setGranularityPeriod(-1); }
catch (MonitorSettingException e) { ... }
```

## 3. 开源代码实际用法调研
- JDK 源码自用位（来源：`~/git/jdk8u-dev/jdk/src/share/classes/javax/management/monitor/`）。
- 说明：纯异常载体，测试聚焦构造器、消息传递与继承链。

## 4. 测试用例设计
- 白盒：具体类、继承 JMRuntimeException、父类直指 JMRuntimeException、2 个 public 构造器。
- 黑盒：String 构造器 message 原样返回；无参构造器 message 为 null；instanceof JMRuntimeException/RuntimeException。

## 深化（2026-11 批 I：monitor/timer 真闭环）

### 新增覆盖维度
- **构造链**：无参（message null）、`(String)`（message 原样）、经反射确认仅此两个 public 构造器；instanceof JMRuntimeException/RuntimeException/Exception。
- **序列化**：svuid 锁定 `-8807913418190202007`；round-trip 后类型与 message 保持。
- **错误路径不抛实证**：真实触发全部四类监控错误（属性缺失/对象缺失/类型错/getter 抛异常）——错误**从不**以 MonitorSettingException 形式抛出，而是转成 error.* 通知；双侧 JDK 源码（jdk8u-dev 与 jdk27u）中该类均无 throw 点，属规约保留位（记录性结论）。

### 实测锁定（JDK-8 与 JDK-27 完全一致）
- svuid、构造器集合、异常层级完全相同；无任何版本差异。
