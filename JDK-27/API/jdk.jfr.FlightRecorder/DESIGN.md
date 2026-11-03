# jdk.jfr.FlightRecorder 设计案

## 1. API 功能
JFR 单例入口：isAvailable/getFlightRecorder/getEventTypes/getRecordings。

## 2. Java 用户如何使用
FlightRecorder.getFlightRecorder().getEventTypes() 列出事件类型。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/FlightRecorder.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 isAvailable + 事件类型非空 + recordings 列表。
