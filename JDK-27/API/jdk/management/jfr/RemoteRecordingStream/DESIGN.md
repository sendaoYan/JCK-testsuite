# jdk.management.jfr.RemoteRecordingStream 设计案

## 1. API 功能
远程录制流（implements EventStream）：setMaxAge/setMaxSize/onEvent/close。

## 2. Java 用户如何使用
new RemoteRecordingStream(MBeanServerConnection)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management.jfr/share/classes/jdk/management/jfr/RemoteRecordingStream.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+配置+close。
