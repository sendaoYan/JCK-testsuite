# javax.sound.midi.Sequence — DESIGN

## 1. API 功能
`Sequence` 是包含时间信息与多条 `Track` 的 MIDI 数据结构。方法分组：
- 常量：`PPQ=0.0f`、`SMPTE_24=24.0f`、`SMPTE_25=25.0f`、`SMPTE_30DROP=29.97f`、`SMPTE_30=30.0f`。
- 构造：`Sequence(float divisionType, int resolution)`、`Sequence(float, int, int numTracks)`（非法 divisionType 抛 `InvalidMidiDataException`）。
- 查询：`getDivisionType()`、`getResolution()`、`getTracks()`、`getTickLength()`、`getMicrosecondLength()`、`getPatchList()`。
- 轨道管理：`createTrack()`、`deleteTrack(Track)`。

## 2. Java 用户如何使用
`Sequence s = new Sequence(Sequence.PPQ, 96)` → `Track t = s.createTrack()` → `t.add(new MidiEvent(new ShortMessage(...), 96L))` → `s.getTickLength()`（=96）→ `s.getMicrosecondLength()`（96 拍按默认 120BPM=500000us）→ `s.deleteTrack(t)`。
SMPTE 序列：`new Sequence(Sequence.SMPTE_25, 40)`，tick 1000 时 `getMicrosecondLength()==1000000`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/Sequence.java`：构造器只接受五种 divisionType，否则抛 `InvalidMidiDataException("Unsupported division type: ...")`；`getTickLength()` 取各 track `ticks()` 的最大值；`getMicrosecondLength()` 委托 `com.sun.media.sound.MidiUtils.tick2microsecond`；`getPatchList()` 当前实现返回 `new Patch[0]`。
- `src/java.desktop/share/classes/com/sun/media/sound/MidiUtils.java`：`DEFAULT_TEMPO_MPQ = 500000`（120bpm），`tick2microsecond` 对 PPQ 用 `tick * tempo / resolution`，对 SMPTE 用 `tick / (divisionType * resolution)`。
- `test/jdk/javax/sound/midi/Sequence/GetMicrosecondLength.java`：构造 PPQ 序列、`createTrack()`、加 tempo meta 事件后验证 `getMicrosecondLength()`。

## 4. 测试用例设计 + 自动化实现（`SequenceTest.testBlackBox`）
- 五个计时常量精确值断言。
- PPQ 构造：divisionType/resolution/初始 0 轨道/tickLength=0/microsecondLength=0/patchList 空。
- `createTrack` 后轨道数+实例一致；加事件后 `getTickLength()==96`、`getMicrosecondLength()==500000`。
- `new Sequence(PPQ,96,3)` 三轨道；`deleteTrack` 成员 true、非成员 false。
- SMPTE_25×40 序列 tick=1000 时 tickLength=1000、microsecondLength=1000000。
- 非法 divisionType 抛 `InvalidMidiDataException`。
