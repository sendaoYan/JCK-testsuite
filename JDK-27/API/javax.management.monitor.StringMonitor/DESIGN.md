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
