# javax.sound.midi.Transmitter — DESIGN

## 1. API 功能
`Transmitter` 是发送 MIDI 消息的接口，继承 `java.lang.AutoCloseable`。方法：`setReceiver(Receiver receiver)`（注册接收端，可传 null 取消）、`getReceiver()`（返回当前接收端）、`close()`（释放资源）。

## 2. Java 用户如何使用
用户通过 `MidiSystem.getTransmitter()` 或 `MidiSystem.getSequencer().getTransmitter()` 获得发送端，`setReceiver(receiver)` 注册接收端，处理完 `close()`。典型组合链：`Transmitter t = sequencer.getTransmitter(); t.setReceiver(myReceiver); ... t.getReceiver(); t.close();`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/MidiSystem.java`：`getTransmitter()` 返回默认发送设备，并包成 `MidiDeviceTransmitter`。
- `src/java.desktop/share/classes/javax/sound/midi/MidiDevice.java`：`getTransmitter()` 定义设备级发送端。
- `test/jdk/javax/sound/midi/Transmitter/bug6415669.java`：验证 `Transmitter` 的接收端注册/关闭行为。
- `test/jdk/javax/sound/midi/Devices/MidiIO.java`：真实设备上 `getTransmitter()`/`setReceiver`/`getReceiver` I/O 回路。

## 4. 测试用例设计 + 自动化实现（`TransmitterTest.testBlackBox`）
- 白盒：断言 isInterface、继承 `AutoCloseable`、3 个方法 `setReceiver(Receiver)`、`getReceiver()`、`close()`。
- 黑盒：通过 `MidiSystem.getSequencer().getTransmitter()` 获得真实发送端（不可用时降级为仅白盒）；用本地 `Receiver` 桩验证 `setReceiver/getReceiver` 往返与 `setReceiver(null)`；`close()` 不抛异常。
