# jdk.management.jfr.RecordingInfo 设计案（JDK-8）

## 1. API 功能
录制的值对象快照：id/name/state/dumpOnExit/size/toDisk/maxAge/maxSize/startTime/stopTime/destination/duration/settings + static `from(CompositeData)`。

## 2. Java 用户如何使用
`FlightRecorderMXBean.getRecordings()` 的元素；远程 JMX 通道经 CompositeData 重建。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.management.jfr/share/classes/jdk/management/jfr/RecordingInfo.java（8u262 backport）。

## 4. 测试用例设计 + 自动化实现
白盒 + 黑盒（NEW→RUNNING→STOPPED 全 getter + MXBean 行形状 + from() 双通道）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 8 → 42。
### 行为闭环
- 三态全 getter：NEW（settings>100、destination null、toDisk true、时间戳 0）→ RUNNING（startTime>0）→ STOPPED（stopTime>=startTime）。
- MXBean 行形状：13 项含 `toDisk`（注意：行键是 toDisk 而选项键是 disk），id 项类型 LONG。
### 实测负例异常表（8 侧 from() 双负例）
| 操作 | 异常 | 精确消息 |
|---|---|---|
| `RecordingInfo.from(自家 MXBean 的行)` | ClassCastException | `java.lang.Long cannot be cast to java.lang.Integer`（from() 源码 `(int) cd.get("id")`，而 MXBean 发 Long——backport 缺陷，稳定复现） |
| `from(手工旧线形状)` settings TabularData | （非异常：塌缩） | 236 条 settings 塌缩为单条 `{key=<最后一行键>}`（与 27 相同怪癖） |
### 手工旧线形状正例
id 用 Integer、布尔键用旧名 `disk`：from() 全字段重建成功（id 7/name/state/size 42/toDisk true/时间戳/destination null）。
### 8/27 差异
27 侧 from() 已修复为 `(long)`——可正常消费自家 MXBean 行；8 侧只能走手工 Integer id 通道。
