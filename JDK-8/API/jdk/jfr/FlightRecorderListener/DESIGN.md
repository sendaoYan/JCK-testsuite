# jdk.jfr.FlightRecorderListener 设计案（JDK-8 (temurin jdk8u372-b07)）

## 1. API 功能
监听器接口：recorderInitialized(FlightRecorder)、recordingStateChanged(Recording)。两方法均为 default 实现（8 侧 javap 不显示 default 标志但 isDefault()==true，27 同）。

## 2. Java 用户如何使用
实现接口并 addListener；已初始化 recorder 上注册会立即同步收到 recorderInitialized。

## 3. 参考来源
- ~/git/jdk/test/jdk/jdk/jfr/（官方用例：api/recording、api/event、api/metadata/eventtype、api/metadata/annotationelement、api/metadata/valuedescriptor、api/flightrecorder、api/settings 等）
- ~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/*.java（权威实现）
- jdk.jfr 包 javadoc + 双侧 javap -public 逐方法核对（JDK-8 jdk8u372-b07 / JDK-27 jdk-27.0.1.0.2+1）

## 4. 测试用例设计 + 自动化实现
白盒（反射结构断言）+ 黑盒（真实行为断言）合并在 FlightRecorderListenerTest.java；static int passed / static void check 骨架，末尾唯一一次 PASS 打印；othervm 独立 JVM；事件名唯一（test.FlightRecorderListener.*）；有界轮询≤2s；临时文件 finally 清理。

### 深化（2026-11-05，契约覆盖）
- 接口面：恰好 2 方法、各 1 参数、均 default（双侧一致，isDefault 实证）。
- 回调到达：init 立即（同步于 addListener）；RUNNING/STOPPED/CLOSED 有界等待(≤2s)；recorderInitialized 收到单例本体；recordingStateChanged 收到我们的 Recording 实例。
- removeListener 幂等（重复移除不抛）。
负例：无（本接口无异常契约）。
