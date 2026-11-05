# jdk.management.jfr.FlightRecorderMXBean 设计案

## 1. API 功能
JFR 管理 MXBean 接口：newRecording/start/stop/close/get*。

## 2. Java 用户如何使用
ManagementFactory.getPlatformMXBean 获取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management.jfr/share/classes/jdk/management/jfr/FlightRecorderMXBean.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒获取+newRecording→start→stop→close 链路。

## 5. 深化（本轮）
断言规模：基线 22 → 76。
### 行为闭环
- 双通道全序闭环（ManagementFactory 直连 + MBeanServer invoke）：newRecording → setPredefinedConfiguration("default") → startRecording → copyTo 落盘 → stopRecording → openStream/readStream 到 EOF（有界循环，MBean 开放签名 `(long, javax.management.openmbean.TabularData)`）→ closeStream → cloneRecording → closeRecording。
- 未知 id 987654 矩阵：全操作 IAE（深度解包 MBeanServer 包装异常链后断言 IAE 且消息含 id）。
- 配置负例：坏配置文本 IAE `Could not parse`；未知选项 IAE `Unknown recording option`。
- snapshot：takeSnapshot id > 0、STOPPED 态、stopRecording(snapshot) ISE。
- 27 特有面：newRecording 等不声明 SecurityException；valid options 含 destination（8 无）；ObjectAllocationSample 在列（8 缺席）。
### 实测负例异常表
| 操作 | 异常 | 精确消息 |
|---|---|---|
| 录制操作用不存在 id 987654 | IllegalArgumentException | `No recording available with id 987654`（消息含 id） |
| `setConfiguration` 坏文本 | IllegalArgumentException | `Could not parse ...` |
| `setRecordingOptions` 未知选项 | IllegalArgumentException | `Unknown recording option: ...` |
| stopRecording(snapshot/已停止) | IllegalStateException | `Can't stop an already stopped recording.` |
### 8/27 差异
见各差异行：SecurityException 声明、destination 选项、FileRead 设置数量与顺序、ObjectAllocationSample、二次 stop 语义。
