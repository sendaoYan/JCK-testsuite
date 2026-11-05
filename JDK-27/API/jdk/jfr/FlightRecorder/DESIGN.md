# jdk.jfr.FlightRecorder 设计案

## 1. API 功能
JFR 单例入口：isAvailable/getFlightRecorder/getEventTypes/getRecordings。

## 2. Java 用户如何使用
FlightRecorder.getFlightRecorder().getEventTypes() 列出事件类型。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/FlightRecorder.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 isAvailable + 事件类型非空 + recordings 列表。

## 5. 深化（2026-11-05）：契约覆盖扩展
- 单例同一性（两次 getFlightRecorder ==）；isAvailable()==true；isInitialized false→true 转变。
- 注册状态机：getEventType(cls) 隐式注册；unregister 后 getEventTypes 不含且 getEventType(cls)→ISE；register 恢复。
- 注册表成员：jdk.ThreadStart/jdk.NativeMethodSample 双侧都在；版本差异：jdk.ObjectAllocationSample 27 有 8 无（两侧各断真值）。
- getRecordings 含新建 Recording、close 后移除；takeSnapshot state==STOPPED。
- addListener：已初始化 recorder 上 addListener 立即同步回调 recorderInitialized（参数==单例）；recordingStateChanged 有界等待(≤2s)见 RUNNING/STOPPED/CLOSED；removeListener 幂等。
- addPeriodicEvent + period "10 ms" + 录制中：周期 runnable 有界等待实测触发≥1 次；removePeriodicEvent true→再次 false。
- 版本差异（反射异常声明）：8 侧 getFlightRecorder/addPeriodicEvent/removePeriodicEvent 声明 throws SecurityException，27 侧不声明——两侧各自断言。
负例精确异常表：getEventType 于 unregister 后→IllegalStateException "is not registered"。
