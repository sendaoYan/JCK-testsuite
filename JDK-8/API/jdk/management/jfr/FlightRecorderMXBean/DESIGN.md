# jdk.management.jfr.FlightRecorderMXBean 设计案（JDK-8）

## 1. API 功能
FlightRecorder 的管理 MXBean：录制生命周期（newRecording/start/stop/close/clone）、配置（setConfiguration/setPredefinedConfiguration）、设置（get/setRecordingOptions、get/setRecordingSettings）、快照（takeSnapshot）、流（openStream/readStream/closeStream/copyTo）。

## 2. Java 用户如何使用
`ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class)` 或远程 MBeanServer 通道驱动录制（JMC/运维面板）。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.management.jfr/share/classes/jdk/management/jfr/FlightRecorderMXBean.java（8u262 backport，rt/ext 类路径形态）。
- ObjectName `jdk.management.jfr:type=FlightRecorder`。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（ManagementFactory 直连 + MBeanServer invoke 双通道全序闭环）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 12 → 72。
### 行为闭环
- 直连全序：newRecording → getRecordingOptions 默认值（6 项：disk/dumpOnExit/duration/maxAge/maxSize/name）→ setPredefinedConfiguration("default")（settings 236 条）→ setRecordingSettings 子集（全量替换语义）→ startRecording → getRecordings RUNNING → copyTo 落盘非空 → stopRecording → STOPPED → closeRecording → 列表移除。
- MBeanServer 通道同序：newRecording/setPredefinedConfiguration/startRecording/copyTo/stopRecording/openStream+readStream 到 EOF（有界循环）+closeStream/closeRecording，openStream 的 MBean 开放签名为 `(long, javax.management.openmbean.TabularData)`。
- takeSnapshot：id>0、name=="Snapshot"、state==STOPPED（8 实测）、stopRecording(snapshot) 抛 ISE。
- 事件类型面：>100 条（实测 129）；jdk.FileRead 3 设置且顺序 [enabled, threshold, stackTrace]；jdk.ObjectAllocationSample 缺席。
- 配置面：default+profile 在列。
### 实测负例异常表（全部直连实测）
| 操作 | 异常 | 精确消息 |
|---|---|---|
| 任一录制操作用不存在 id 987654（getRecordingOptions/start/stop/close/openStream/copyTo/setPredefinedConfiguration） | IllegalArgumentException | `No recording available with id 987654` |
| `setPredefinedConfiguration(id, "no-such-config")` | IllegalArgumentException | （消息非空，含配置名语义） |
| `setConfiguration(id, "this is not xml")` | IllegalArgumentException | `Could not parse configuration`（cause 链含 ParseException/SAXParseException） |
| `setRecordingOptions` 未知选项 nosuchoption | IllegalArgumentException | `Unknown recording option: nosuchoption. Valid options are [dumpOnExit, duration, name, maxAge, maxSize, disk].` |
| `setRecordingOptions` 设 destination | IllegalArgumentException | `Unknown recording option: destination. ...`（8 侧无此选项） |
| 对 STOPPED 录制再 stopRecording | IllegalStateException | `Can't stop an already stopped recording.` |
| MBeanServer 通道 ghost id | RuntimeMBeanException → IllegalArgumentException | 同直连消息 |
### 8/27 差异
- 8 声明 SecurityException（newRecording/clone/start/stop/copyTo 五方法）；27 不声明。
- 8 选项键为 `disk` 且无 `destination`；27 有 `toDisk`+`destination`。
- 8 FileRead 3 设置（无 throttle）；27 4 设置且 threshold 居首。
- 8 二次 stopRecording 抛 ISE；27 侧返回 false 语义（实测另案）。
- 8 ObjectAllocationSample 缺席；27 在列。
