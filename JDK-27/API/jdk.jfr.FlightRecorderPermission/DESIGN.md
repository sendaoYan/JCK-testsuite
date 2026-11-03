# jdk.jfr.FlightRecorderPermission 设计案

## 1. API 功能
JFR 权限（extends BasicPermission）：accessFlightRecorder/registerEvent 等。

## 2. Java 用户如何使用
new FlightRecorderPermission("accessFlightRecorder").implies(...)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/FlightRecorderPermission.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 final/extends BasicPermission；黑盒 getName/implies/equals/hashCode。
