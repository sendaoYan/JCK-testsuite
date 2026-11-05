# jdk.management.jfr.RecordingInfo 设计案

## 1. API 功能
JFR 录制信息：id/name/state/settings/start/stop。

## 2. Java 用户如何使用
FlightRecorderMXBean.getRecordings()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management.jfr/share/classes/jdk/management/jfr/RecordingInfo.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 newRecording 后查询。

## 5. 深化（本轮）
断言规模：基线 21 → 46。
### 行为闭环
- 三态全 getter：NEW（settings>1、destination null、toDisk true、时间戳 0）→ RUNNING（startTime>0）→ STOPPED（stopTime>=startTime）；closeRecording 后移除。
- MXBean 行通道：Recordings 属性取行 → `from(row)` 13 项标量全等价（id/state/name/destination/dumpOnExit/maxAge/maxSize/duration/size/toDisk/startTime/stopTime）。
### 实测负例异常表（settings 塌缩怪癖，双侧一致）
| 操作 | 现象 | 精确行为 |
|---|---|---|
| `from(row).getSettings()` | 非异常、丢数据 | 381 条 settings 塌缩为单条 `{"key": <最后一行键>}`——from() 用 TabularData 索引名 "key" 作 map 键（openjdk RecordingInfo(CompositeData) 实现，8/27 相同） |
### 8/27 差异
8 的 from() 消费自家 MXBean 行直接 CCE（Long→Integer）；27 已修复可正常往返标量。
