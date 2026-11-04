# javax.sound.midi.MidiDeviceReceiver — DESIGN

## 1. API 功能
`MidiDeviceReceiver` 是 `Receiver` 的子接口，额外标识接收端所属的 `MidiDevice`。方法：`MidiDevice getMidiDevice()`（返回提供该接收端的 MIDI 设备）；继承 `Receiver` 的 `send(MidiMessage, long)` 与 `close()`。

## 2. Java 用户如何使用
用户通过 `MidiDevice.getReceiver()` 或 `MidiSystem.getReceiver()` 获得实现 `MidiDeviceReceiver` 的接收端，用 `getMidiDevice()` 查询所属设备。典型组合链：`Receiver r = MidiSystem.getReceiver(); if (r instanceof MidiDeviceReceiver mdr) { MidiDevice dev = mdr.getMidiDevice(); } mdr.send(msg, -1); mdr.close();`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/MidiSystem.java`：`getReceiver()` 若返回的不是 `MidiDeviceReceiver` 则包成 `MidiDeviceReceiverEnvelope`，保证总是 `MidiDeviceReceiver`。
- `src/java.desktop/share/classes/javax/sound/midi/MidiDeviceReceiverEnvelope.java`：内部实现持有所属设备与委托接收端。
- `test/jdk/javax/sound/midi/Devices/MidiDeviceGetReceivers.java`：遍历设备 `getReceiver()` 并校验 `MidiDeviceReceiver.getMidiDevice()`。
- `src/java.desktop/share/classes/com/sun/media/sound/SoftReceiver.java`：Gervill 软合成器的接收端实现。

## 4. 测试用例设计 + 自动化实现（`MidiDeviceReceiverTest.testBlackBox`）
- 白盒：断言 isInterface、继承 `Receiver`、仅 1 个方法 `getMidiDevice()` 返回 `MidiDevice` 且无参。
- 黑盒：通过 `MidiSystem.getReceiver()` 获得真实 `MidiDeviceReceiver`（不可用时降级为仅白盒）；`getMidiDevice()` 非空且多次调用同一实例；继承的 `send`/`close` 不抛异常。
