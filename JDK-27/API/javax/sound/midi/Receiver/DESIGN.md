# javax.sound.midi.Receiver — DESIGN

## 1. API 功能
`Receiver` 是接收 MIDI 消息的接口，继承 `java.lang.AutoCloseable`（可 try-with-resources）。方法：`void send(MidiMessage message, long timeStamp)`（发送消息，时间戳单位由上下文决定）、`void close()`（释放资源）。

## 2. Java 用户如何使用
用户通过 `MidiSystem.getReceiver()` 获得接收端，构造 `ShortMessage`/`MetaMessage` 后 `send(msg, -1)` 发送，最后 `close()` 或 try-with-resources 自动关闭。典型组合链：`Receiver r = MidiSystem.getReceiver(); ShortMessage m = new ShortMessage(); m.setMessage(NOTE_ON, 0, 60, 100); r.send(m, -1); r.close();`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/midi/MidiSystem.java`：`getReceiver()` 返回默认接收设备（Gervill 软合成器）的 `Receiver`，并包成 `MidiDeviceReceiver`。
- `src/java.desktop/share/classes/javax/sound/midi/MidiDevice.java`：`getReceiver()` 定义设备级接收端。
- `test/jdk/javax/sound/midi/Gervill/SoftReceiver/Send_NoteOn.java` 等：通过 `MidiSystem.getReceiver()`/`SoftReceiver` 发送各类 `ShortMessage`。
- `test/jdk/javax/sound/midi/Devices/MidiIO.java`：真实设备上 `getReceiver()`/`send`/`close` 的 I/O 回路。

## 4. 测试用例设计 + 自动化实现（`ReceiverTest.testBlackBox`）
- 白盒：断言 isInterface、继承 `AutoCloseable`、2 个方法 `send(MidiMessage,long)` 与 `close()`。
- 黑盒：通过 `MidiSystem.getReceiver()` 获得真实接收端（不可用时降级为仅白盒）；构造 `ShortMessage`/`MetaMessage` 并 `send`；`close()` 不抛异常；另用 try-with-resources 验证 `AutoCloseable` 契约。
