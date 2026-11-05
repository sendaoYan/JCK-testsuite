# javax.sound.midi.MidiEvent — DESIGN

## 1. API 功能
`MidiEvent` 封装一个 `MidiMessage` 及其 tick 时间戳（以 MIDI tick 为单位），是 `Sequence`/`Track` 中存储的事件单元。方法：`MidiEvent(MidiMessage message, long tick)`、`getMessage()`（返回同一消息引用）、`setTick(long)`、`getTick()`。

## 2. Java 用户如何使用
用户 `new MidiEvent(shortMessage, tick)` 放入 `Track`，或从 `Track.get(i).getMessage()` 取回。典型组合链：构造 `ShortMessage` → `setMessage(NOTE_ON, 0, 60, 100)` → `new MidiEvent(msg, 0)` → `getTick()`/`setTick(480)` → `getMessage()` 取回同一实例。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `test/jdk/javax/sound/midi/ShortMessage/FastShortMessage.java`：`ShortMessage msg = new ShortMessage(); msg.setMessage(...); track.add(new MidiEvent(msg, 0));`，读回时 `(ShortMessage) track.get(0).getMessage()`。
- `src/java.desktop/share/classes/javax/sound/midi/MidiEvent.java`：`message` 字段 `final`、`tick` 可变，`getMessage()` 直接返回引用。
- `src/java.desktop/share/classes/javax/sound/midi/Track.java`：`add(MidiEvent)`、`get(int)`、`size()`、`remove(MidiEvent)` 以 `MidiEvent` 为单元。
- 大量 test/jdk 用例（如 `Track/TrackAddSameTick.java`、`Sequence` 系列）通过 `MidiEvent` 构造并校验 tick。

## 4. 测试用例设计 + 自动化实现（`MidiEventTest.testBlackBox`）
- 白盒：断言非 abstract、继承 `Object`、`message` 字段 `final`、`tick` 字段可变、方法 `getMessage/setTick/getTick`。
- 黑盒：`getMessage()` 返回同一 `ShortMessage`/`MetaMessage` 引用；`setTick/getTick` 往返（含 0、负数、`Long.MAX_VALUE`）；多个事件共享同一消息但 tick 状态独立。
