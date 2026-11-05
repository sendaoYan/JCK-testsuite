# jdk.jfr.RecordingState 设计案（JDK-8 (temurin jdk8u372-b07)）

## 1. API 功能
录制状态枚举 {NEW, DELAYED, RUNNING, STOPPED, CLOSED}，定义录制生命周期全序。

## 2. Java 用户如何使用
Recording.getState() 返回；状态转移 NEW→(DELAYED)→RUNNING→STOPPED→CLOSED。

## 3. 参考来源
- ~/git/jdk/test/jdk/jdk/jfr/（官方用例：api/recording、api/event、api/metadata/eventtype、api/metadata/annotationelement、api/metadata/valuedescriptor、api/flightrecorder、api/settings 等）
- ~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/*.java（权威实现）
- jdk.jfr 包 javadoc + 双侧 javap -public 逐方法核对（JDK-8 jdk8u372-b07 / JDK-27 jdk-27.0.1.0.2+1）

## 4. 测试用例设计 + 自动化实现
白盒（反射结构断言）+ 黑盒（真实行为断言）合并在 RecordingStateTest.java；static int passed / static void check 骨架，末尾唯一一次 PASS 打印；othervm 独立 JVM；事件名唯一（test.RecordingState.*）；有界轮询≤2s；临时文件 finally 清理。

### 深化（2026-11-05，契约覆盖）
- values() 精确顺序+长度 5；ordinal 0..4；name/toString；compareTo 全序；valueOf 往返。
- values() 每次返回新数组（改副本不影响枚举本体）。
- Enum.valueOf 泛型通道与类特定 valueOf 一致。
- 真实 Recording 生命周期逐步核对枚举取值（NEW→RUNNING→STOPPED→CLOSED）。
负例精确异常表：valueOf("bogus")→IllegalArgumentException；valueOf(null)→NullPointerException。
