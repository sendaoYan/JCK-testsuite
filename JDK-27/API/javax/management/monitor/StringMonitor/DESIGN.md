# javax.management.monitor.StringMonitor — 设计案

## 1. API 功能
- 具体类，继承 `Monitor` 并实现 `StringMonitorMBean`。
- 核心属性：`StringToCompare`、`NotifyMatch`、`NotifyDiffer`、`DerivedGauge`。
- 当观测属性等于/不等于比较串时，分别发 `STRING_TO_COMPARE_VALUE_MATCHED` / `STRING_TO_COMPARE_VALUE_DIFFERED`。
- 部分方法已废弃（getStringToCompare/setStringToCompare 等）。

## 2. Java 用户如何使用
监控一个字符串属性是否匹配期望值。
```java
StringMonitor sm = new StringMonitor();
sm.setStringToCompare("hello");
sm.setNotifyMatch(true);
sm.setObservedObject(name);
sm.setObservedAttribute("Message");
sm.start();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/monitor/StringMonitorTest.java` 覆盖比较串、通知开关与派生量。
- 说明：测试聚焦无启动线程情况下的状态往返与校验。

## 4. 测试用例设计
- 白盒：非接口、继承 Monitor、实现 StringMonitorMBean。
- 黑盒：默认比较串为空、通知开关 false、派生量为 null；setStringToCompare/getStringToCompare、setNotifyMatch/Differ；observed object/attribute/granularity；通知信息含 matched/differed；异常路径（setStringToCompare(null)）。

## 深化（2026-11 批 I：monitor/timer 真闭环）

### 新增覆盖维度（本地 MBeanServer 真调度）
- **differ/match 闭环**：stringToCompare="GO"、初始值 "INIT" → 首个轮询即 `jmx.monitor.string.differs`；改为 "GO" → `jmx.monitor.string.matches`（trigger=="GO"）；再改 "STOP" → differs。derivedGauge 始终等于当前观测串。
- **开关矩阵**：notifyDiffer/notifyMatch 四组合——关 differ 后改串不通知但派生量照常更新；两开关全关则完全静默。
- **校验**：setStringToCompare(null) → IAE（null 串拒绝）；空串 "" 合法且照常比较。

### 实测锁定（JDK-8 与 JDK-27 完全一致）
- 观测 MBean 骨架的访问器必须**配对命名**（getStr/setStr 同一属性 "Str"）：getter 与 setter 名不配对时该属性对监控器而言不存在/只写，读属性 → `jmx.monitor.error.attribute` 而非 differ/matches（本批实测踩坑点）。
