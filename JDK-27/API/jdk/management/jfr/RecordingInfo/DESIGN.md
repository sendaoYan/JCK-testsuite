# jdk.management.jfr.RecordingInfo 设计案

## 1. API 功能
JFR 录制信息：id/name/state/settings/start/stop。

## 2. Java 用户如何使用
FlightRecorderMXBean.getRecordings()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management.jfr/share/classes/jdk/management/jfr/RecordingInfo.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 newRecording 后查询。
