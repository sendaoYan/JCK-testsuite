# javax.sound.midi.MidiDeviceTransmitter — DESIGN

## 1. API 功能
`MidiDeviceTransmitter` 是 `Transmitter` 的子接口，额外标识发送端所属的 `MidiDevice`。方法：`MidiDevice getMidiDevice()`（返回提供该发送端的 MIDI 设备）；继承 `Transmitter` 的 `setReceiver(Receiver)`、`getReceiver()` 与 `close()`。

## 2. Java 用户如何使用
用户通过 `MidiDevice.getTransmitter()` 或 `MidiSystem.getTransmitter()` 获得实现 `MidiDeviceTransmitter` 的发送端，用 `getMidiDevice()` 查询所属设备。典型组合链：`Transmitter t = sequencer.getTransmitter(); if (t instanceof MidiDeviceTransmitter mdt) { MidiDevice dev = mdt.getMidiDevice(); } t.setReceiver(receiver); t.close();`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/MidiSystem.java`：`getTransmitter()` 若返回的不是 `MidiDeviceTransmitter` 则包成 `MidiDeviceTransmitterEnvelope`。
- `src/java.desktop/share/classes/javax/sound/midi/MidiDeviceTransmitterEnvelope.java`：内部实现持有所属设备与委托发送端。
- `test/jdk/javax/sound/midi/Devices/MidiIO.java`、`MidiDeviceGetReceivers.java`：遍历设备 `getTransmitter()` 并校验 `getMidiDevice()`。
- `src/java.desktop/share/classes/com/sun/media/sound/RealTimeSequencer.java`：其内部 `SequencerTransmitter` 实现 `MidiDeviceTransmitter`。

## 4. 测试用例设计 + 自动化实现（`MidiDeviceTransmitterTest.testBlackBox`）
- 白盒：断言 isInterface、继承 `Transmitter`、仅 1 个方法 `getMidiDevice()` 返回 `MidiDevice` 且无参。
- 黑盒：通过 `MidiSystem.getSequencer().getTransmitter()` 获得真实 `MidiDeviceTransmitter`（不可用时降级为仅白盒）；`getMidiDevice()` 非空且多次调用同一实例；继承的 `setReceiver/getReceiver` 往返与 `close()`。
