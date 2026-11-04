# jdk.management.jfr.FlightRecorderMXBean 设计案

## 1. API 功能
JFR 管理 MXBean 接口：newRecording/start/stop/close/get*。

## 2. Java 用户如何使用
ManagementFactory.getPlatformMXBean 获取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management.jfr/share/classes/jdk/management/jfr/FlightRecorderMXBean.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒获取+newRecording→start→stop→close 链路。
