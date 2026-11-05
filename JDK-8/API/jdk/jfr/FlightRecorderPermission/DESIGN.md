# jdk.jfr.FlightRecorderPermission 设计案（JDK-8 (temurin jdk8u372-b07)）

## 1. API 功能
JFR 权限类：BasicPermission 子类，目标名 accessFlightRecorder / registerEvent（27 起 @Deprecated(forRemoval=true)）。

## 2. Java 用户如何使用
new FlightRecorderPermission("accessFlightRecorder")；SecurityManager 场景下守护 getFlightRecorder/register。

## 3. 参考来源
- ~/git/jdk/test/jdk/jdk/jfr/（官方用例：api/recording、api/event、api/metadata/eventtype、api/metadata/annotationelement、api/metadata/valuedescriptor、api/flightrecorder、api/settings 等）
- ~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/*.java（权威实现）
- jdk.jfr 包 javadoc + 双侧 javap -public 逐方法核对（JDK-8 jdk8u372-b07 / JDK-27 jdk-27.0.1.0.2+1）

## 4. 测试用例设计 + 自动化实现
白盒（反射结构断言）+ 黑盒（真实行为断言）合并在 FlightRecorderPermissionTest.java；static int passed / static void check 骨架，末尾唯一一次 PASS 打印；othervm 独立 JVM；事件名唯一（test.FlightRecorderPermission.*）；有界轮询≤2s；临时文件 finally 清理。

### 深化（2026-11-05，契约覆盖）
- 两个合法名构造+getName 回读；implies 同名 true/异名 false/异类同名 false；getActions 空；equals/hashCode；Serializable；newPermissionCollection 非空。
- 版本差异：27 侧 @Deprecated(forRemoval=true)（断言注解在且 forRemoval）；8 侧无 deprecation（断言缺席）。
负例精确异常表：null name→NullPointerException；"bogus"→IllegalArgumentException "name: bogus"。
