# jdk.jfr.FlightRecorderListener 设计案

## 1. API 功能
JFR 监听器接口，默认方法 recorderInitialized/recordingStateChanged。

## 2. Java 用户如何使用
匿名实现覆盖 recordingStateChanged 接收状态变更。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/FlightRecorderListener.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface/方法存在；黑盒 匿名实现调用默认方法不抛异常。
