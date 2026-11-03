# javax.sound.midi.Sequencer — DESIGN

## 1. API 功能
`Sequencer` 是播放 `Sequence` 的接口（extends `MidiDevice`）。方法分组：
- 序列：`setSequence(Sequence/InputStream)`、`getSequence()`。
- 播放/录制：`start()`、`stop()`、`isRunning()`、`startRecording()`、`stopRecording()`、`isRecording()`、`recordEnable(Track,int)`、`recordDisable(Track)`。
- 速度：`getTempoInBPM()`、`setTempoInBPM(float)`、`getTempoInMPQ()`、`setTempoInMPQ(float)`、`setTempoFactor(float)`、`getTempoFactor()`。
- 时间：`getTickLength()`、`getTickPosition()`、`setTickPosition(long)`、`getMicrosecondLength()`、`getMicrosecondPosition()`、`setMicrosecondPosition(long)`。
- 同步：`setMasterSyncMode/getMasterSyncMode/getMasterSyncModes`、`setSlaveSyncMode/getSlaveSyncMode/getSlaveSyncModes`。
- 轨道静音/独奏：`setTrackMute/getTrackMute`、`setTrackSolo/getTrackSolo`。
- 监听器：`addMetaEventListener/removeMetaEventListener`、`addControllerEventListener/removeControllerEventListener`。
- 循环：`setLoopStartPoint/getLoopStartPoint`、`setLoopEndPoint/getLoopEndPoint`、`setLoopCount/getLoopCount`（常量 `LOOP_CONTINUOUSLY`）。

## 2. Java 用户如何使用
`Sequencer s = MidiSystem.getSequencer()` → `s.setSequence(sequence)` → `s.setTempoInBPM(120)` → `s.open()` → `s.start()` → `s.isRunning()` → `s.stop()` → `s.close()`。
`getSequencer(false)` 返回不自动连接合成器的实例；无默认设备时抛 `MidiUnavailableException`，需降级。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/Sequencer.java`：接口含 `SyncMode` 内部类；`getSequence()` 规范说明未设置时返回 `null`。
- `test/jdk/javax/sound/midi/Sequencer/ToString.java`：`new Sequencer.SyncMode("nameToTest"){}` 后断言 `toString()`。
- `test/jdk/javax/sound/midi/Sequencer/SetTickPosition.java`：`setTickPosition/getTickPosition` 往返。

## 4. 测试用例设计 + 自动化实现（`SequencerTest.testBlackBox`）
- `getSequencer(false)` 获取实例（`MidiUnavailableException` 时降级 return）。
- `setSequence/getSequence` 往返、`isRunning/isRecording` 初值、`setTempoInBPM(120)`/`setTempoInMPQ(600000)`/`setTempoFactor(1.0)` 往返。
- `getTickLength()==96`、`getMicrosecondLength()==500000`（事件 tick=96）。
- `open()` 后 `setTickPosition(10)` 往返、`getMicrosecondPosition()>=0`。
- master/slave 同步模式非空且数组非空。
- 循环：默认 `getLoopCount()==0`，`setLoopStartPoint(0)/setLoopEndPoint(96)/setLoopCount(2)` 往返。
- `setTrackMute(0,true)/getTrackMute(0)`、`setTrackSolo(0,true)/getTrackSolo(0)`。
- `start()` 后 `isRunning()`、`stop()` 后 `!isRunning()`；finally 中 `close()`。
