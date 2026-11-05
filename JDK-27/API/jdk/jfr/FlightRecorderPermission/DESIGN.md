# jdk.jfr.FlightRecorderPermission 设计案

## 1. API 功能
JFR 权限（extends BasicPermission）：accessFlightRecorder/registerEvent 等。

## 2. Java 用户如何使用
new FlightRecorderPermission("accessFlightRecorder").implies(...)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/FlightRecorderPermission.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 final/extends BasicPermission；黑盒 getName/implies/equals/hashCode。

## 5. 深化（2026-11-05）：契约覆盖扩展
- 两个合法名构造+getName 回读；implies 同名 true/异名 false/异类同名 false；getActions 空；equals/hashCode；Serializable；newPermissionCollection 非空。
- 版本差异：27 侧 @Deprecated(forRemoval=true)（断言注解在且 forRemoval）；8 侧无 deprecation（断言缺席）。
负例精确异常表：null name→NullPointerException；"bogus"→IllegalArgumentException "name: bogus"。
