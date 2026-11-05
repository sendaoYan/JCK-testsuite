# jdk.jfr.FlightRecorder 设计案（JDK-8 (temurin jdk8u372-b07)）

## 1. API 功能
JFR 全局单例入口：getFlightRecorder/isAvailable/isInitialized、事件类型注册表（register/unregister/getEventTypes）、录制列表、takeSnapshot、监听器管理、周期事件（add/removePeriodicEvent）。

## 2. Java 用户如何使用
FlightRecorder.getFlightRecorder() 单例；EventType.getEventType(cls) 触发隐式注册；register/unregister 控制可见性；takeSnapshot 抓非持久快照。

## 3. 参考来源
- ~/git/jdk/test/jdk/jdk/jfr/（官方用例：api/recording、api/event、api/metadata/eventtype、api/metadata/annotationelement、api/metadata/valuedescriptor、api/flightrecorder、api/settings 等）
- ~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/*.java（权威实现）
- jdk.jfr 包 javadoc + 双侧 javap -public 逐方法核对（JDK-8 jdk8u372-b07 / JDK-27 jdk-27.0.1.0.2+1）

## 4. 测试用例设计 + 自动化实现
白盒（反射结构断言）+ 黑盒（真实行为断言）合并在 FlightRecorderTest.java；static int passed / static void check 骨架，末尾唯一一次 PASS 打印；othervm 独立 JVM；事件名唯一（test.FlightRecorder.*）；有界轮询≤2s；临时文件 finally 清理。

### 深化（2026-11-05，契约覆盖）
- 单例同一性（两次 getFlightRecorder ==）；isAvailable()==true；isInitialized false→true 转变。
- 注册状态机：getEventType(cls) 隐式注册；unregister 后 getEventTypes 不含且 getEventType(cls)→ISE；register 恢复。
- 注册表成员：jdk.ThreadStart/jdk.NativeMethodSample 双侧都在；版本差异：jdk.ObjectAllocationSample 27 有 8 无（两侧各断真值）。
- getRecordings 含新建 Recording、close 后移除；takeSnapshot state==STOPPED。
- addListener：已初始化 recorder 上 addListener 立即同步回调 recorderInitialized（参数==单例）；recordingStateChanged 有界等待(≤2s)见 RUNNING/STOPPED/CLOSED；removeListener 幂等。
- addPeriodicEvent + period "10 ms" + 录制中：周期 runnable 有界等待实测触发≥1 次；removePeriodicEvent true→再次 false。
- 版本差异（反射异常声明）：8 侧 getFlightRecorder/addPeriodicEvent/removePeriodicEvent 声明 throws SecurityException，27 侧不声明——两侧各自断言。
负例精确异常表：getEventType 于 unregister 后→IllegalStateException "is not registered"。
